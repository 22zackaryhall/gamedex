/****************************************************************************
 * Copyright (C) 2016-2018 Yevgeny Krasik                                   *
 *                                                                          *
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 *                                                                          *
 * http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                          *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 ****************************************************************************/

package com.gitlab.ykrasik.gamedex.app.javafx.filter

import com.gitlab.ykrasik.gamedex.Library
import com.gitlab.ykrasik.gamedex.app.api.filter.Filter
import com.gitlab.ykrasik.gamedex.app.api.game.GameFilterView
import com.gitlab.ykrasik.gamedex.app.api.image.Image
import com.gitlab.ykrasik.gamedex.app.api.image.ViewWithProviderLogos
import com.gitlab.ykrasik.gamedex.app.api.util.IsValid
import com.gitlab.ykrasik.gamedex.app.api.util.ValueOrError
import com.gitlab.ykrasik.gamedex.app.api.util.channel
import com.gitlab.ykrasik.gamedex.app.javafx.image.image
import com.gitlab.ykrasik.gamedex.javafx.*
import com.gitlab.ykrasik.gamedex.javafx.control.*
import com.gitlab.ykrasik.gamedex.javafx.view.PresentableView
import com.gitlab.ykrasik.gamedex.provider.ProviderId
import com.gitlab.ykrasik.gamedex.util.FileSize
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import kotlinx.coroutines.channels.Channel
import org.joda.time.LocalDate
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*
import kotlin.reflect.KClass

/**
 * User: ykrasik
 * Date: 27/01/2018
 * Time: 13:07
 */
class JavaFxGameFilterView(override val onlyShowConditionsForCurrentPlatform: Boolean) : PresentableView(), GameFilterView, ViewWithProviderLogos {
    override val possibleGenres = mutableListOf<String>()
    override val possibleTags = mutableListOf<String>()
    override val possibleLibraries = mutableListOf<Library>()
    override val possibleProviderIds = mutableListOf<ProviderId>()
    override val possibleRules = mutableListOf<KClass<out Filter.Rule>>().observable()

    val filterChanges = channel<Filter>()
    val filterProperty = SimpleObjectProperty<Filter>(Filter.`true`).eventOnChange(filterChanges)
    override var filter by filterProperty

    val isValid: ObjectProperty<IsValid> = SimpleObjectProperty(IsValid.valid)

    override val wrapInAndActions = channel<Filter>()
    override val wrapInOrActions = channel<Filter>()
    override val wrapInNotActions = channel<Filter>()
    override val unwrapNotActions = channel<Filter.Not>()
    override val clearFilterActions = channel<Unit>()
    override val updateFilterActions = channel<Pair<Filter.Rule, Filter.Rule>>()
    override val replaceFilterActions = channel<Pair<Filter, KClass<out Filter>>>()
    override val deleteFilterActions = channel<Filter>()

    override var providerLogos = emptyMap<ProviderId, Image>()

    private var indent = 0

    private val periodFormatter = PeriodFormatterBuilder()
        .appendYears().appendSuffix(" years").appendSeparator(" ")
        .appendMonths().appendSuffix(" months").appendSeparator(" ")
        .appendDays().appendSuffix(" days").appendSeparator(" ")
        .appendHours().appendSuffix(" hours").appendSeparator(" ")
        .appendMinutes().appendSuffix(" minutes").appendSeparator(" ")
        .appendSeconds().appendSuffix(" seconds")
        .toFormatter()

    override val root = vbox {
        fun rerender() = replaceChildren {
            isValid.unbind()
            isValid.value = IsValid.valid
            render(filterProperty.value, Filter.`true`)
        }
        filterProperty.onChange { rerender() }
        possibleRules.onChange { rerender() }
    }

    init {
        viewRegistry.onCreate(this)
    }

    private fun EventTarget.render(filter: Filter, parentFilter: Filter) {
        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (filter) {
            is Filter.BinaryOperator -> renderOperator(filter, parentFilter) {
                render(filter.left, filter)
                render(filter.right, filter)
            }
            is Filter.UnaryOperator -> render(filter.target, filter)
            is Filter.Rule -> renderBasicRule(filter, parentFilter, possibleRules) {
                val ruleProperty = when (filter) {
                    is Filter.Platform -> renderPlatformFilter(filter)
                    is Filter.Library -> renderLibraryFilter(filter)
                    is Filter.Genre -> renderGenreFilter(filter)
                    is Filter.Tag -> renderTagFilter(filter)
                    is Filter.Provider -> renderProviderFilter(filter)
                    is Filter.CriticScore -> renderScoreFilter(filter, Filter::CriticScore)
                    is Filter.NullCriticScore -> renderNullFilter(filter)
                    is Filter.UserScore -> renderScoreFilter(filter, Filter::UserScore)
                    is Filter.NullUserScore -> renderNullFilter(filter)
                    is Filter.AvgScore -> renderScoreFilter(filter, Filter::AvgScore)
                    is Filter.NullAvgScore -> renderNullFilter(filter)
                    is Filter.MinScore -> renderScoreFilter(filter, Filter::MinScore)
                    is Filter.MaxScore -> renderScoreFilter(filter, Filter::MaxScore)
                    is Filter.TargetReleaseDate -> renderTargetDateFilter(filter, Filter::TargetReleaseDate)
                    is Filter.PeriodReleaseDate -> renderPeriodDateFilter(filter, Filter::PeriodReleaseDate)
                    is Filter.NullReleaseDate -> renderNullFilter(filter)
                    is Filter.TargetCreateDate -> renderTargetDateFilter(filter, Filter::TargetCreateDate)
                    is Filter.PeriodCreateDate -> renderPeriodDateFilter(filter, Filter::PeriodCreateDate)
                    is Filter.TargetUpdateDate -> renderTargetDateFilter(filter, Filter::TargetUpdateDate)
                    is Filter.PeriodUpdateDate -> renderPeriodDateFilter(filter, Filter::PeriodUpdateDate)
                    is Filter.FileSize -> renderFileSizeFilter(filter)
                    else -> filter.toProperty()
                }
                ruleProperty.eventOnChange(updateFilterActions) { filter to it }
            }
            else -> kotlin.error("Unknown filter: $filter")
        }
    }

    private fun EventTarget.renderOperator(operator: Filter.Operator, parentFilter: Filter, op: VBox.() -> Unit) = vbox(spacing = 2.0) {
        addClass(Style.borderedContent)
        renderBasicRule(operator, parentFilter, possible = listOf(Filter.And::class, Filter.Or::class))

        indent += 1
        op(this)
        indent -= 1
    }

    private fun EventTarget.renderBasicRule(
        filter: Filter,
        parentFilter: Filter,
        possible: List<KClass<out Filter>>,
        op: HBox.() -> Unit = {}
    ) = defaultHbox {
        useMaxWidth = true

        gap(size = indent * 10.0)

        val descriptor = filter.descriptor
        buttonWithPopover(descriptor.selectedName, descriptor.selectedIcon().size(26), closeOnClick = false) { popOver ->
            // TODO: As an optimization, can share this popover accross all menus.
            fun EventTarget.conditionButton(descriptor: ConditionDisplayDescriptor) =
                jfxButton(descriptor.name, descriptor.icon().size(26), alignment = Pos.CENTER_LEFT) {
                    useMaxWidth = true
                    action {
                        popOver.hide()
                        replaceFilterActions.event(filter to descriptor.condition)
                    }
                }

            val subMenus = mutableMapOf<ConditionDisplaySubMenu, VBox>()
            fun subMenu(descriptor: ConditionDisplaySubMenu, f: VBox.() -> Unit) = f(subMenus.getOrPut(descriptor) {
                lateinit var vbox: VBox
                subMenu(descriptor.text, descriptor.icon().size(26)) { vbox = this }
                vbox
            })

            var needGap = false
            possible.forEach {
                if (needGap) {
                    verticalGap(size = 20)
                }

                val newDescriptor = it.descriptor
                if (newDescriptor.subMenu != null) {
                    subMenu(newDescriptor.subMenu) {
                        conditionButton(newDescriptor)
                    }
                } else {
                    conditionButton(newDescriptor)
                }

                needGap = newDescriptor.gap
            }
        }.apply {
            minWidth = 120.0
        }

        val negated = parentFilter is Filter.Not
        val target = if (negated) parentFilter else filter
        jfxButton(graphic = (if (negated) descriptor.negatedActionIcon() else descriptor.actionIcon()).size(26)) {
            tooltip("Reverse this condition")
            isDisable = filter is Filter.True
            action {
                when {
                    !negated -> wrapInNotActions.event(target)
                    negated -> unwrapNotActions.event(target as Filter.Not)
                }
            }
        }

        op()

        spacer()
        gap()
        renderAdditionalButtons(target)
    }

    private fun HBox.renderAdditionalButtons(filter: Filter) {
        // FIXME: Use a NodeList
        buttonWithPopover(graphic = Icons.siteMap.size(28)) {
            fun operatorButton(name: String, graphic: Node, channel: Channel<Filter>) = jfxButton(name, graphic, alignment = Pos.CENTER_LEFT) {
                useMaxWidth = true
                eventOnAction(channel) { filter }
            }

            operatorButton("And", Icons.and, wrapInAndActions)
            operatorButton("Or", Icons.or, wrapInOrActions)
        }.apply {
            tooltip("Add boolean operator")
        }
        deleteButton {
            removeClass(CommonStyle.toolbarButton)
            // Do not allow deleting an empty root
            isDisable = filter === this@JavaFxGameFilterView.filter && filter is Filter.True
            eventOnAction(deleteFilterActions) { filter }
        }
    }

    private fun HBox.renderPlatformFilter(rule: Filter.Platform) = rule.toProperty().apply {
        val platform = mapBidirectional(Filter.Platform::platform, Filter::Platform)
        platformComboBox(platform)
    }

    private fun HBox.renderProviderFilter(rule: Filter.Provider) = rule.toProperty().apply {
        val provider = mapBidirectional(Filter.Provider::providerId, Filter::Provider)
        popoverComboMenu(
            possibleItems = possibleProviderIds,
            selectedItemProperty = provider,
            text = { it },
            graphic = { providerLogos[it]!!.image.toImageView(height = 28) }
        )
    }

    private fun HBox.renderLibraryFilter(rule: Filter.Library) = rule.toProperty().apply {
        val library = mapBidirectional({ possibleLibraries.find { it.id == id }!! }, { Filter.Library(id) })
        popoverComboMenu(
            possibleItems = possibleLibraries,
            selectedItemProperty = library,
            text = { it.name },
            graphic = { it.platform.logo }
        )
    }

    private fun HBox.renderGenreFilter(rule: Filter.Genre) = rule.toProperty().apply {
        val genre = mapBidirectional(Filter.Genre::genre, Filter::Genre)
        popoverComboMenu(
            possibleItems = possibleGenres,
            selectedItemProperty = genre
        )
    }

    private fun HBox.renderTagFilter(rule: Filter.Tag) = rule.toProperty().apply {
        val tag = mapBidirectional(Filter.Tag::tag, Filter::Tag)
        popoverComboMenu(
            possibleItems = possibleTags,
            selectedItemProperty = tag
        )
    }

    private fun HBox.renderTargetDateFilter(rule: Filter.TargetDate, factory: (LocalDate) -> Filter.TargetDate) = rule.toProperty().apply {
        tooltip("Is after the given date")
        val date = mapBidirectional(Filter.TargetDate::date, factory)
        jfxDatePicker(date)
    }

    private fun HBox.renderPeriodDateFilter(rule: Filter.PeriodDate, factory: (Period) -> Filter.PeriodDate) = rule.toProperty().apply {
        tooltip("Is within a duration ago from now")
        jfxTextField(periodFormatter.print(rule.period)) {
            val period = bindParser {
                try {
                    periodFormatter.parsePeriod(it)
                } catch (e: Exception) {
                    kotlin.error("${e.message} [years months days hours minutes seconds]")
                }
            }
            bindValidation(period)
            period.onChange {
                if (it!!.isSuccess) {
                    this@apply.value = factory(it.value!!)
                }
            }
        }
    }

    private fun HBox.renderScoreFilter(rule: Filter.TargetScore, factory: (Double) -> Filter.TargetScore) = rule.toProperty().apply {
        defaultHbox {
            val target = mapBidirectional({ target }, factory)
            bindValidation(numberTextField(target, min = 0, max = 100))
        }
    }

    private fun renderNullFilter(rule: Filter.Rule) = rule.toProperty()

    private fun bindValidation(validation: ObservableValue<out ValueOrError<Any>>) {
        isValid.cleanBind(isValid.and(validation))
    }

    private fun HBox.renderFileSizeFilter(filter: Filter.FileSize) = filter.toProperty().apply {
        val (initialAmount, initialScale) = filter.target.scaled
        val amountProperty = SimpleIntegerProperty(initialAmount.toInt())
        val scaleProperty = SimpleObjectProperty(initialScale)
        plusMinusSlider(amountProperty, min = 1, max = 999)
        enumComboMenu(scaleProperty)
        amountProperty.combineLatest(scaleProperty).onChange {
            val (amount, scale) = it!!
            this@apply.value = Filter.FileSize(FileSize(amount, scale))
        }
    }

    private data class ConditionDisplayDescriptor(
        val condition: KClass<out Filter>,
        val name: String,
        val icon: () -> FontIcon,
        val gap: Boolean = false,
        val actionIcon: () -> FontIcon = Icons::checked,
        val negatedActionIcon: () -> FontIcon = Icons::checkX,
        val subMenu: ConditionDisplaySubMenu? = null
    ) {
        val selectedName = subMenu?.text ?: name
        val selectedIcon = subMenu?.icon ?: icon
    }

    private data class ConditionDisplaySubMenu(
        val text: String,
        val icon: () -> FontIcon
    )

    private companion object {
        private val criticScoreSubMenu = ConditionDisplaySubMenu("Critic Score", Icons::starFull)
        private val userScoreSubMenu = ConditionDisplaySubMenu("User Score", Icons::starEmpty)
        private val avgScoreSubMenu = ConditionDisplaySubMenu("Average Score", Icons::starHalf)
        private val releaseDateSubMenu = ConditionDisplaySubMenu("Release Date", Icons::date)
        private val createDateSubMenu = ConditionDisplaySubMenu("Create Date", Icons::createDate)
        private val updateDateSubMenu = ConditionDisplaySubMenu("Update Date", Icons::updateDate)

        private inline fun <reified T : Filter.TargetScore> score(subMenu: ConditionDisplaySubMenu?, gap: Boolean = false) =
            ConditionDisplayDescriptor(T::class, "Is bigger than", Icons::gtOrEq, gap, Icons::gtOrEq, Icons::lt, subMenu)

        private inline fun <reified T : Filter.TargetDate> targetDate(subMenu: ConditionDisplaySubMenu, gap: Boolean = false) =
            ConditionDisplayDescriptor(T::class, "Is after the given date", Icons::gtOrEq, gap, Icons::gtOrEq, Icons::lt, subMenu)

        private inline fun <reified T : Filter.PeriodDate> periodDate(subMenu: ConditionDisplaySubMenu, gap: Boolean = false) =
            ConditionDisplayDescriptor(T::class, "Is in a time period ending now", Icons::clockStart, gap, Icons::clockStart, Icons::clockEnd, subMenu)

        private inline fun <reified T : Filter> nullCondition(subMenu: ConditionDisplaySubMenu, gap: Boolean = false) =
            ConditionDisplayDescriptor(T::class, "Is null", Icons::checkX, gap = gap, actionIcon = Icons::checkX, negatedActionIcon = Icons::checked, subMenu = subMenu)

        val displayDescriptors = listOf(
            ConditionDisplayDescriptor(Filter.Not::class, "Not", Icons::exclamation),
            ConditionDisplayDescriptor(Filter.And::class, "And", Icons::and),
            ConditionDisplayDescriptor(Filter.Or::class, "Or", Icons::or),
            ConditionDisplayDescriptor(Filter.True::class, "Select Condition", Icons::select),
            ConditionDisplayDescriptor(Filter.Platform::class, "Platform", Icons::computer, gap = true),
            ConditionDisplayDescriptor(Filter.Library::class, "Library", { Icons.hdd.color(Color.BLACK) }),
            ConditionDisplayDescriptor(Filter.Genre::class, "Genre", Icons::documents),
            ConditionDisplayDescriptor(Filter.Tag::class, "Tag", { Icons.tag.color(Color.BLACK) }),
            ConditionDisplayDescriptor(Filter.Provider::class, "Provider", Icons::database, gap = true),
            score<Filter.CriticScore>(criticScoreSubMenu),
            nullCondition<Filter.NullCriticScore>(criticScoreSubMenu),
            score<Filter.UserScore>(userScoreSubMenu),
            nullCondition<Filter.NullUserScore>(userScoreSubMenu),
            score<Filter.AvgScore>(avgScoreSubMenu),
            nullCondition<Filter.NullAvgScore>(avgScoreSubMenu),
            ConditionDisplayDescriptor(Filter.MinScore::class, "Min Score", Icons::min, actionIcon = Icons::gtOrEq, negatedActionIcon = Icons::lt),
            ConditionDisplayDescriptor(Filter.MaxScore::class, "Max Score", Icons::max, gap = true, actionIcon = Icons::gtOrEq, negatedActionIcon = Icons::lt),
            targetDate<Filter.TargetReleaseDate>(releaseDateSubMenu),
            periodDate<Filter.PeriodReleaseDate>(releaseDateSubMenu),
            nullCondition<Filter.NullReleaseDate>(releaseDateSubMenu),
            targetDate<Filter.TargetCreateDate>(createDateSubMenu),
            periodDate<Filter.PeriodCreateDate>(createDateSubMenu),
            targetDate<Filter.TargetUpdateDate>(updateDateSubMenu),
            periodDate<Filter.PeriodUpdateDate>(updateDateSubMenu, gap = true),
            ConditionDisplayDescriptor(Filter.FileSize::class, "File Size", Icons::fileQuestion, gap = true, actionIcon = Icons::gtOrEq, negatedActionIcon = Icons::lt),
            ConditionDisplayDescriptor(Filter.Duplications::class, "Duplications", Icons::duplicate),
            ConditionDisplayDescriptor(Filter.NameDiff::class, "Folder-Game Name Diff", Icons::diff)
        ).map { it.condition to it }.toMap()
    }

    private val Filter.descriptor get() = this::class.descriptor
    private val KClass<out Filter>.descriptor get() = displayDescriptors[this]!!

    class Style : Stylesheet() {
        companion object {
            val borderedContent by cssclass()

            init {
                importStylesheetSafe(Style::class)
            }
        }

        init {
            borderedContent {
                padding = box(2.px)
                borderColor = multi(box(Color.BLACK))
                borderWidth = multi(box(0.5.px))
            }
        }
    }
}
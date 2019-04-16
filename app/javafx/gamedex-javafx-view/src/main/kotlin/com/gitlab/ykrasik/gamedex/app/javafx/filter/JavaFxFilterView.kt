/****************************************************************************
 * Copyright (C) 2016-2019 Yevgeny Krasik                                   *
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
import com.gitlab.ykrasik.gamedex.app.api.UserMutableState
import com.gitlab.ykrasik.gamedex.app.api.filter.Filter
import com.gitlab.ykrasik.gamedex.app.api.filter.FilterView
import com.gitlab.ykrasik.gamedex.app.api.util.channel
import com.gitlab.ykrasik.gamedex.app.javafx.common.JavaFxCommonOps
import com.gitlab.ykrasik.gamedex.javafx.combineLatest
import com.gitlab.ykrasik.gamedex.javafx.control.*
import com.gitlab.ykrasik.gamedex.javafx.importStylesheetSafe
import com.gitlab.ykrasik.gamedex.javafx.state
import com.gitlab.ykrasik.gamedex.javafx.theme.*
import com.gitlab.ykrasik.gamedex.javafx.view.PresentableView
import com.gitlab.ykrasik.gamedex.provider.ProviderId
import com.gitlab.ykrasik.gamedex.util.Extractor
import com.gitlab.ykrasik.gamedex.util.FileSize
import com.gitlab.ykrasik.gamedex.util.IsValid
import javafx.beans.Observable
import javafx.beans.property.Property
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import kotlinx.coroutines.channels.Channel
import org.joda.time.DurationFieldType
import org.joda.time.LocalDate
import org.joda.time.Period
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*
import kotlin.reflect.KClass

/**
 * User: ykrasik
 * Date: 27/01/2018
 * Time: 13:07
 */
class JavaFxFilterView(override val onlyShowFiltersForCurrentPlatform: Boolean) : PresentableView(), FilterView {
    override val possibleGenres = mutableListOf<String>()
    override val possibleTags = mutableListOf<String>()
    override val possibleLibraries = mutableListOf<Library>()
    override val possibleProviderIds = mutableListOf<ProviderId>().observable()
    override val possibleRules = mutableListOf<KClass<out Filter.Rule>>().observable()

    override val setFilterActions = channel<Filter>()
    override val wrapInAndActions = channel<Filter>()
    override val wrapInOrActions = channel<Filter>()
    override val wrapInNotActions = channel<Filter>()
    override val unwrapNotActions = channel<Filter.Not>()
    override val clearFilterActions = channel<Unit>()
    override val updateFilterActions = channel<Pair<Filter.Rule, Filter.Rule>>()
    override val replaceFilterActions = channel<Pair<Filter, KClass<out Filter>>>()
    override val deleteFilterActions = channel<Filter>()

    override val filter = state<Filter>(Filter.Null)
    override val filterIsValid = state(IsValid.valid)

    private val commonOps: JavaFxCommonOps by di()

    private var indent = 0

    override val root = vbox {
        // TODO: Re-rendering like this is possibly leaking listeners.
        fun rerender() = replaceChildren {
            render(filter.property.value, Filter.Null)
        }
        filter.property.onChange { rerender() }
        possibleRules.onChange { rerender() }
    }

    init {
        register()
    }

    val externalMutations = object : UserMutableState<Filter> {
        private var prevExternalFilter: Filter? = null

        override var value: Filter
            get() = filter.value
            set(value) {
                prevExternalFilter = value
                filter.value = value
                setFilterActions.offer(value)
            }

        override val changes = channel<Filter>()

        init {
            filter.property.addListener { _: Observable ->
                val value = filter.value
                if (value !== prevExternalFilter) {
                    changes.offer(value)
                }
                prevExternalFilter = null
            }
        }
    }

    private fun EventTarget.render(filter: Filter, parentFilter: Filter) {
        when (filter) {
            is Filter.BinaryOperator -> renderOperator(filter, parentFilter) {
                render(filter.left, filter)
                render(filter.right, filter)
            }
            is Filter.UnaryOperator -> render(filter.target, filter)
            is Filter.Rule -> renderBasicRule(filter, parentFilter, possibleRules) {
                when (filter) {
                    is Filter.Platform -> renderPlatformFilter(filter)
                    is Filter.Library -> renderLibraryFilter(filter)
                    is Filter.Genre -> renderGenreFilter(filter)
                    is Filter.Tag -> renderTagFilter(filter)
                    is Filter.Provider -> renderProviderFilter(filter)
                    is Filter.CriticScore -> renderScoreFilter(filter, Filter::CriticScore)
                    is Filter.NullCriticScore -> renderNullFilter()
                    is Filter.UserScore -> renderScoreFilter(filter, Filter::UserScore)
                    is Filter.NullUserScore -> renderNullFilter()
                    is Filter.AvgScore -> renderScoreFilter(filter, Filter::AvgScore)
                    is Filter.MinScore -> renderScoreFilter(filter, Filter::MinScore)
                    is Filter.MaxScore -> renderScoreFilter(filter, Filter::MaxScore)
                    is Filter.TargetReleaseDate -> renderTargetDateFilter(filter, Filter::TargetReleaseDate)
                    is Filter.PeriodReleaseDate -> renderPeriodDateFilter(filter, Filter::PeriodReleaseDate)
                    is Filter.NullReleaseDate -> renderNullFilter()
                    is Filter.TargetCreateDate -> renderTargetDateFilter(filter, Filter::TargetCreateDate)
                    is Filter.PeriodCreateDate -> renderPeriodDateFilter(filter, Filter::PeriodCreateDate)
                    is Filter.TargetUpdateDate -> renderTargetDateFilter(filter, Filter::TargetUpdateDate)
                    is Filter.PeriodUpdateDate -> renderPeriodDateFilter(filter, Filter::PeriodUpdateDate)
                    is Filter.FileSize -> renderFileSizeFilter(filter)
                    else -> filter.toProperty()
                }
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
        buttonWithPopover(descriptor.selectedName, descriptor.selectedIcon().size(26), onClickBehavior = PopOverOnClickBehavior.Ignore) {
            // TODO: As an optimization, can share this popover accross all menus.
            fun EventTarget.filterButton(descriptor: FilterDisplayDescriptor) =
                jfxButton(descriptor.name, descriptor.icon().size(26), alignment = Pos.CENTER_LEFT) {
                    useMaxWidth = true
                    action {
                        popOver.hide()
                        replaceFilterActions.event(filter to descriptor.filter)
                    }
                }

            val subMenus = mutableMapOf<FilterDisplaySubMenu, VBox>()
            fun subMenu(descriptor: FilterDisplaySubMenu, f: VBox.() -> Unit) = f(subMenus.getOrPut(descriptor) {
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
                        filterButton(newDescriptor)
                    }
                } else {
                    filterButton(newDescriptor)
                }

                needGap = newDescriptor.gap
            }
        }.apply {
            minWidth = 120.0
        }

        val negated = parentFilter is Filter.Not
        val target = if (negated) parentFilter else filter
        jfxButton(graphic = (if (negated) descriptor.negatedActionIcon() else descriptor.actionIcon()).size(26)) {
            tooltip("Reverse this filter")
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
        buttonWithPopover(graphic = Icons.lan.size(28)) {
            fun operatorButton(name: String, graphic: Node, channel: Channel<Filter>) = jfxButton(name, graphic, alignment = Pos.CENTER_LEFT) {
                useMaxWidth = true
                action(channel) { filter }
            }

            operatorButton("And", Icons.and, wrapInAndActions)
            operatorButton("Or", Icons.or, wrapInOrActions)
        }.apply {
            tooltip("Add boolean operator")
        }
        deleteButton {
            removeClass(CommonStyle.toolbarButton)
            // Do not allow deleting an empty root
            isDisable = filter === this@JavaFxFilterView.filter && filter is Filter.True
            action(deleteFilterActions) { filter }
        }
    }

    private fun HBox.renderPlatformFilter(filter: Filter.Platform) {
        val platform = filter.toProperty(Filter.Platform::platform, Filter::Platform)
        platformComboBox(platform)
    }

    private fun HBox.renderProviderFilter(filter: Filter.Provider) {
        val provider = filter.toProperty(Filter.Provider::providerId, Filter::Provider)
        popoverComboMenu(
            possibleItems = possibleProviderIds,
            selectedItemProperty = provider,
            text = { it },
            graphic = { commonOps.providerLogo(it).toImageView(height = 28) }
        )
    }

    private fun HBox.renderLibraryFilter(filter: Filter.Library) {
        // There is a race filter when switching platforms with a library filter active -
        // The actual filter & the possible rules are updated by different presenters, which can lead
        // to situations where the possible libraries already point to platform specific libraries
        // but the filter is still the filter for the old platform, which will mean the library isn't found.
        // Currently there's no other solution but to tolerate this situation, as the presenter in charge of
        // the platform filter will call us just a bit later and fix it.
        val library = filter.toProperty(
            { possibleLibraries.find { it.id == id } ?: Library.Null },
            { Filter.Library(it.id) }
        )
        popoverComboMenu(
            possibleItems = possibleLibraries,
            selectedItemProperty = library,
            text = { it.name },
            graphic = { it.platformOrNull?.logo }
        )
    }

    private fun HBox.renderGenreFilter(filter: Filter.Genre) {
        val genre = filter.toProperty(Filter.Genre::genre, Filter::Genre)
        popoverComboMenu(
            possibleItems = possibleGenres,
            selectedItemProperty = genre
        )
    }

    private fun HBox.renderTagFilter(filter: Filter.Tag) {
        val tag = filter.toProperty(Filter.Tag::tag, Filter::Tag)
        popoverComboMenu(
            possibleItems = possibleTags,
            selectedItemProperty = tag
        )
    }

    private fun HBox.renderTargetDateFilter(filter: Filter.TargetDate, factory: (LocalDate) -> Filter.TargetDate) {
        tooltip("Is after the given date")
        val date = filter.toProperty(Filter.TargetDate::date, factory)
        jfxDatePicker(date)
    }

    private fun HBox.renderPeriodDateFilter(filter: Filter.PeriodDate, factory: (Period) -> Filter.PeriodDate) {
        tooltip("Is within a duration ago from now")
        val initialPeriodType = PeriodType.values().firstOrNull { it.extractor(filter.period) > 0 } ?: PeriodType.Months
        val initialAmount = initialPeriodType.extractor(filter.period)
        val periodTypeProperty = initialPeriodType.toProperty()
        val amountProperty = initialAmount.toProperty()
        plusMinusSlider(amountProperty, min = 0, max = 100)
        enumComboMenu(periodTypeProperty)
        amountProperty.combineLatest(periodTypeProperty).bindChanges(updateFilterActions) { (amount, periodType) ->
            filter to factory(Period().withField(periodType.fieldType, amount.toInt()))
        }
    }

    private fun HBox.renderScoreFilter(filter: Filter.TargetScore, factory: (Double) -> Filter.TargetScore) {
        val target = filter.toProperty(Filter.TargetScore::score, factory)
        plusMinusSlider(target, min = 0, max = 100)
    }

    private fun HBox.renderFileSizeFilter(filter: Filter.FileSize) {
        val (initialAmount, initialScale) = filter.target.scaled
        val amountProperty = SimpleIntegerProperty(initialAmount.toInt())
        val scaleProperty = SimpleObjectProperty(initialScale)
        plusMinusSlider(amountProperty, min = 1, max = 999)
        enumComboMenu(scaleProperty)
        amountProperty.combineLatest(scaleProperty).bindChanges(updateFilterActions) { (amount, scale) ->
            filter to Filter.FileSize(FileSize(amount, scale))
        }
    }

    private fun renderNullFilter() {}

    private inline fun <Rule : Filter.Rule, T : Any> Rule.toProperty(
        extractor: Extractor<Rule, T>,
        crossinline factory: (T) -> Rule
    ): Property<T> = extractor(this).toProperty().bindChanges(updateFilterActions) { this to factory(it) }

    enum class PeriodType(val fieldType: DurationFieldType, val extractor: (Period) -> Int) {
        Years(DurationFieldType.years(), Period::getYears),
        Months(DurationFieldType.months(), Period::getMonths),
        Days(DurationFieldType.days(), Period::getDays),
        Hours(DurationFieldType.hours(), Period::getHours),
        Minutes(DurationFieldType.minutes(), Period::getMinutes),
        Seconds(DurationFieldType.seconds(), Period::getSeconds);

        operator fun invoke(period: Period): Int = extractor(period)
    }

    private data class FilterDisplayDescriptor(
        val filter: KClass<out Filter>,
        val name: String,
        val icon: () -> FontIcon,
        val gap: Boolean = false,
        val actionIcon: () -> FontIcon = Icons::checked,
        val negatedActionIcon: () -> FontIcon = Icons::checkX,
        val subMenu: FilterDisplaySubMenu? = null
    ) {
        val selectedName = subMenu?.text ?: name
        val selectedIcon = subMenu?.icon ?: icon
    }

    private data class FilterDisplaySubMenu(
        val text: String,
        val icon: () -> FontIcon
    )

    private companion object {
        private val criticScoreSubMenu = FilterDisplaySubMenu("Critic Score", Icons::starFull)
        private val userScoreSubMenu = FilterDisplaySubMenu("User Score", Icons::starEmpty)
        private val releaseDateSubMenu = FilterDisplaySubMenu("Release Date", Icons::date)
        private val createDateSubMenu = FilterDisplaySubMenu("Create Date", Icons::createDate)
        private val updateDateSubMenu = FilterDisplaySubMenu("Update Date", Icons::updateDate)

        private inline fun <reified T : Filter.TargetScore> score(subMenu: FilterDisplaySubMenu?, gap: Boolean = false) =
            FilterDisplayDescriptor(T::class, "Is bigger than", Icons::gtOrEq, gap, Icons::gtOrEq, Icons::lt, subMenu)

        private inline fun <reified T : Filter.TargetDate> targetDate(subMenu: FilterDisplaySubMenu, gap: Boolean = false) =
            FilterDisplayDescriptor(T::class, "Is after the given date", Icons::gtOrEq, gap, Icons::gtOrEq, Icons::lt, subMenu)

        private inline fun <reified T : Filter.PeriodDate> periodDate(subMenu: FilterDisplaySubMenu, gap: Boolean = false) =
            FilterDisplayDescriptor(T::class, "Is in a time period ending now", Icons::clockStart, gap, Icons::clockStart, Icons::clockEnd, subMenu)

        private inline fun <reified T : Filter> nullFilter(subMenu: FilterDisplaySubMenu, gap: Boolean = false) =
            FilterDisplayDescriptor(T::class, "Is null", Icons::checkX, gap = gap, actionIcon = Icons::checkX, negatedActionIcon = Icons::checked, subMenu = subMenu)

        val displayDescriptors = listOf(
            FilterDisplayDescriptor(Filter.Not::class, "Not", Icons::exclamation),
            FilterDisplayDescriptor(Filter.And::class, "And", Icons::and),
            FilterDisplayDescriptor(Filter.Or::class, "Or", Icons::or),
            FilterDisplayDescriptor(Filter.True::class, "Select Filter", Icons::select),
            FilterDisplayDescriptor(Filter.Platform::class, "Platform", Icons::computer, gap = true),
            FilterDisplayDescriptor(Filter.Library::class, "Library", Icons::folder),
            FilterDisplayDescriptor(Filter.Genre::class, "Genre", Icons::script),
            FilterDisplayDescriptor(Filter.Tag::class, "Tag", { Icons.tag.color(Color.BLACK) }),
            FilterDisplayDescriptor(Filter.Provider::class, "Provider", Icons::database, gap = true),
            score<Filter.CriticScore>(criticScoreSubMenu),
            nullFilter<Filter.NullCriticScore>(criticScoreSubMenu),
            score<Filter.UserScore>(userScoreSubMenu),
            nullFilter<Filter.NullUserScore>(userScoreSubMenu),
            FilterDisplayDescriptor(Filter.AvgScore::class, "Average Score", Icons::starHalf, actionIcon = Icons::gtOrEq, negatedActionIcon = Icons::lt),
            FilterDisplayDescriptor(Filter.MinScore::class, "Min Score", Icons::min, actionIcon = Icons::gtOrEq, negatedActionIcon = Icons::lt),
            FilterDisplayDescriptor(Filter.MaxScore::class, "Max Score", Icons::max, gap = true, actionIcon = Icons::gtOrEq, negatedActionIcon = Icons::lt),
            targetDate<Filter.TargetReleaseDate>(releaseDateSubMenu),
            periodDate<Filter.PeriodReleaseDate>(releaseDateSubMenu),
            nullFilter<Filter.NullReleaseDate>(releaseDateSubMenu),
            targetDate<Filter.TargetCreateDate>(createDateSubMenu),
            periodDate<Filter.PeriodCreateDate>(createDateSubMenu),
            targetDate<Filter.TargetUpdateDate>(updateDateSubMenu),
            periodDate<Filter.PeriodUpdateDate>(updateDateSubMenu, gap = true),
            FilterDisplayDescriptor(Filter.FileSize::class, "File Size", Icons::fileQuestion, gap = true, actionIcon = Icons::gtOrEq, negatedActionIcon = Icons::lt),
            FilterDisplayDescriptor(Filter.Duplications::class, "Duplications", Icons::duplicate),
            FilterDisplayDescriptor(Filter.NameDiff::class, "Folder-Game Name Diff", Icons::diff)
        ).map { it.filter to it }.toMap()
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
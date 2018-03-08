package com.gitlab.ykrasik.gamedex.ui.view.settings

import com.gitlab.ykrasik.gamedex.GameProvider
import com.gitlab.ykrasik.gamedex.ProviderUserAccountFeature
import com.gitlab.ykrasik.gamedex.controller.SettingsController
import com.gitlab.ykrasik.gamedex.ui.*
import com.gitlab.ykrasik.gamedex.ui.theme.CommonStyle
import com.gitlab.ykrasik.gamedex.ui.theme.Theme
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.launch
import tornadofx.*

/**
 * User: ykrasik
 * Date: 06/03/2018
 * Time: 10:02
 */
class ProviderUserSettingsFragment(private val provider: GameProvider) : Fragment() {
    private val controller: SettingsController by di()

    private val settings = controller.providerSettings(provider.id)

    private val accountRequired = provider.accountFeature != null
    private val checking = false.toProperty()

    private val stateProperty = when {
        !accountRequired -> State.NotRequired
        settings.account != null -> State.Valid
        else -> State.Empty
    }.toProperty()
    private var state by stateProperty

    private var currentAccount = mapOf<String, String>()
    private var lastVerifiedAccount = settings.account ?: emptyMap()

    override val root = vbox {
        hbox {
            spacer()
            label(provider.id) { addClass(Style.providerLabel) }
            spacer()
        }
        stackpane {
            form {
                disableWhen { checking }
                lateinit var accountLabelFlashContainer: Node
                fieldset {
                    field("Enable") {
                        hbox {
                            alignment = Pos.BASELINE_CENTER
                            jfxToggleButton {
                                isSelected = settings.enable
                                selectedProperty().onChange { selected ->
                                    if (selected && state.isValid || !selected) {
                                        controller.setProviderEnabled(provider.id, enable = selected)
                                    } else {
                                        isSelected = false
                                        accountLabelFlashContainer.flash(target = 0.5, reverse = true)
                                    }
                                }
                            }
                            spacer()
                            stackpane {
                                isVisible = accountRequired
                                label {
                                    addClass(Style.accountLabel)
                                    textProperty().bind(stateProperty.map { it!!.text })
                                    graphicProperty().bind(stateProperty.map { it!!.graphic })
                                    textFillProperty().bind(stateProperty.map { it!!.color })
                                }
                                accountLabelFlashContainer = stackpane { addClass(Style.flashContainer) }
                            }
                        }
                    }
                }
                provider.accountFeature?.let { accountFeature ->
                    fieldset("Account") {
                        accountFields(accountFeature)
                    }
                }
            }
            children += Theme.Images.loading.toImageView().apply {
                visibleWhen { checking }
            }
        }
    }

    private fun Fieldset.accountFields(accountFeature: ProviderUserAccountFeature) {
        accountFeature.fields.forEach { name ->
            field(name) {
                val currentValue = settings.account?.get(name) ?: ""
                currentAccount += name to currentValue
                textfield(currentValue) {
                    textProperty().onChange {
                        currentAccount += name to it!!
                        state = when {
                            it.isEmpty() -> State.Empty
                            currentAccount == lastVerifiedAccount -> State.Valid
                            else -> State.Unverified
                        }
                    }
                }
            }
        }
        hbox {
            spacer()
            jfxButton("Verify Account") {
                addClass(CommonStyle.toolbarButton, CommonStyle.acceptButton, Style.verifyAccountButton)
                disableWhen { stateProperty.isEqualTo(State.Empty) }
                setOnAction {
                    launch(JavaFx) {
                        checking.value = true
                        try {
                            val valid = controller.validateAndUseAccount(provider, currentAccount)
                            state = if (valid) {
                                lastVerifiedAccount = currentAccount
                                State.Valid
                            } else {
                                State.Invalid
                            }
                        } finally {
                            checking.value = false
                        }
                    }
                }
            }
        }
    }

    private enum class State {
        Valid {
            override val isValid = true
            override val text = "Valid Account"
            override val graphic get() = Theme.Icon.accept()
            override val color = Color.GREEN
        },
        Invalid {
            override val isValid = false
            override val text = "Invalid Account"
            override val graphic get() = Theme.Icon.cancel()
            override val color = Color.INDIANRED
        },
        Empty {
            override val isValid = false
            override val text = "No Account"
            override val graphic get() = Theme.Icon.exclamationTriangle().apply { color(Color.ORANGE) }
            override val color = Color.ORANGE
        },
        Unverified {
            override val isValid = false
            override val text = "Unverified Account"
            override val graphic get() = Theme.Icon.exclamationTriangle().apply { color(Color.ORANGE) }
            override val color = Color.ORANGE
        },
        NotRequired {
            override val isValid = true
            override val text = null
            override val graphic = null
            override val color = null
        };

        abstract val isValid: Boolean
        abstract val text: String?
        abstract val graphic: Node?
        abstract val color: Color?
    }

    class Style : Stylesheet() {
        companion object {
            val providerLabel by cssclass()
            val accountLabel by cssclass()
            val flashContainer by cssclass()
            val verifyAccountButton by cssclass()

            init {
                importStylesheet(Style::class)
            }
        }

        init {
            providerLabel {
                fontSize = 20.px
                fontWeight = FontWeight.BOLD
            }

            accountLabel {
                fontSize = 20.px
                fontWeight = FontWeight.BOLD
                padding = box(5.px)
            }

            flashContainer {
                backgroundColor = multi(Color.RED)
                backgroundRadius = multi(box(5.px))
                borderRadius = multi(box(5.px))
                opacity = 0.0
            }

            verifyAccountButton {
                borderColor = multi(box(Color.BLACK))
                borderRadius = multi(box(3.px))
                borderWidth = multi(box(0.5.px))
            }
        }
    }
}
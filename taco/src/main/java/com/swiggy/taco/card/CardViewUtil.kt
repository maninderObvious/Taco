package com.swiggy.taco.card

import com.swiggy.taco.R
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.*
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.AttributeSet
import androidx.annotation.*
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.resources.MaterialResources
import com.google.android.material.ripple.RippleUtils
import com.google.android.material.shape.*

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
internal class CardViewUtil(
        private val materialCardView: Card,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        @StyleRes defStyleRes: Int) {
    val userContentPadding = Rect()

    // Will always wrapped in an InsetDrawable
    val background: MaterialShapeDrawable

    // Will always wrapped in an InsetDrawable
    private val foregroundContentDrawable: MaterialShapeDrawable

    @get:Dimension
    @Dimension
    var checkedIconMargin = 0

    @get:Dimension
    @Dimension
    var checkedIconSize = 0

    @Dimension
    private var strokeWidth = 0

    // If card is clickable, this is the clickableForegroundDrawable otherwise it draws the stroke.
    private var fgDrawable: Drawable? = null
    private var checkedIcon: Drawable? = null
    private var rippleColor: ColorStateList? = null
    private var checkedIconTint: ColorStateList? = null
    var shapeAppearanceModel: ShapeAppearanceModel? = null
        private set
    var strokeColorStateList: ColorStateList? = null
        private set
    private var rippleDrawable: Drawable? = null
    private var clickableForegroundDrawable: LayerDrawable? = null
    private var compatRippleDrawable: MaterialShapeDrawable? = null
    private var foregroundShapeDrawable: MaterialShapeDrawable? = null
    var isBackgroundOverwritten = false
    var isCheckable = false

    @RequiresApi(VERSION_CODES.LOLLIPOP)
    @SuppressLint("RestrictedApi")
    fun loadFromAttributes(attributes: TypedArray) {
        strokeColorStateList = MaterialResources.getColorStateList(
                materialCardView.context,
                attributes,
                R.styleable.MaterialCardView_strokeColor)
        if (strokeColorStateList == null) {
            strokeColorStateList = ColorStateList.valueOf(DEFAULT_STROKE_VALUE)
        }
        strokeWidth = attributes.getDimensionPixelSize(R.styleable.MaterialCardView_strokeWidth, 0)
        isCheckable = attributes.getBoolean(R.styleable.MaterialCardView_android_checkable, false)
        materialCardView.isLongClickable = isCheckable
        checkedIconTint = MaterialResources.getColorStateList(
                materialCardView.context, attributes, R.styleable.MaterialCardView_checkedIconTint)
        setCheckedIcon(
                MaterialResources.getDrawable(
                        materialCardView.context, attributes, R.styleable.MaterialCardView_checkedIcon))
        checkedIconSize = attributes.getDimensionPixelSize(R.styleable.MaterialCardView_checkedIconSize, 0)
        checkedIconMargin = attributes.getDimensionPixelSize(R.styleable.MaterialCardView_checkedIconMargin, 0)
        rippleColor = MaterialResources.getColorStateList(
                materialCardView.context, attributes, R.styleable.MaterialCardView_rippleColor)
        if (rippleColor == null) {
            rippleColor = ColorStateList.valueOf(
                    MaterialColors.getColor(materialCardView, R.attr.colorControlHighlight))
        }
        val foregroundColor = MaterialResources.getColorStateList(
                materialCardView.context,
                attributes,
                R.styleable.MaterialCardView_cardForegroundColor)
        cardForegroundColor = foregroundColor
        updateRippleColor()
        updateElevation()
        updateStroke()
        materialCardView.setBackgroundInternal(insetDrawable(background))
        fgDrawable = if (materialCardView.isClickable) clickableForeground else foregroundContentDrawable
        materialCardView.foreground = insetDrawable(fgDrawable)
    }

    fun setStrokeColor(strokeColor: ColorStateList) {
        if (strokeColorStateList === strokeColor) {
            return
        }
        strokeColorStateList = strokeColor
        updateStroke()
    }

    @ColorInt
    fun getStrokeColor(): Int {
        return if (strokeColorStateList == null) DEFAULT_STROKE_VALUE else strokeColorStateList!!.defaultColor
    }

    fun setStrokeWidth(@Dimension strokeWidth: Int) {
        if (strokeWidth == this.strokeWidth) {
            return
        }
        this.strokeWidth = strokeWidth
        updateStroke()
    }

    @Dimension
    fun getStrokeWidth(): Int {
        return strokeWidth
    }

    var cardBackgroundColor: ColorStateList?
        get() = background.fillColor
        set(color) {
            background.fillColor = color
        }
    var cardForegroundColor: ColorStateList?
        get() = foregroundContentDrawable.fillColor
        set(foregroundColor) {
            foregroundContentDrawable.fillColor = foregroundColor
                    ?: ColorStateList.valueOf(Color.TRANSPARENT)
        }

    fun setUserContentPadding(left: Int, top: Int, right: Int, bottom: Int) {
        userContentPadding[left, top, right] = bottom
        updateContentPadding()
    }

    fun updateClickable() {
        val previousFgDrawable = fgDrawable
        fgDrawable = if (materialCardView.isClickable) clickableForeground else foregroundContentDrawable
        if (previousFgDrawable !== fgDrawable) {
            updateInsetForeground(fgDrawable!!)
        }
    }

    var cornerRadius: Float
        get() = background.topLeftCornerResolvedSize
        set(cornerRadius) {
            setShapeAppearanceModel(shapeAppearanceModel!!.withCornerSize(cornerRadius))
            fgDrawable!!.invalidateSelf()
            if (shouldAddCornerPaddingOutsideCardBackground()
                    || shouldAddCornerPaddingInsideCardBackground()) {
                updateContentPadding()
            }
            if (shouldAddCornerPaddingOutsideCardBackground()) {
                updateInsets()
            }
        }

    @get:FloatRange(from = 0.0, to = 1.0)
    var progress: Float
        get() = background.interpolation
        set(progress) {
            background.interpolation = progress
            if (foregroundContentDrawable != null) {
                foregroundContentDrawable.interpolation = progress
            }
            if (foregroundShapeDrawable != null) {
                foregroundShapeDrawable!!.interpolation = progress
            }
        }

    fun updateElevation() {
        background.elevation = materialCardView.cardElevation
    }

    fun updateInsets() {
        // No way to update the inset amounts for an InsetDrawable, so recreate insets as needed.
        if (!isBackgroundOverwritten) {
            materialCardView.setBackgroundInternal(insetDrawable(background))
        }
        materialCardView.foreground = insetDrawable(fgDrawable)
    }

    fun updateStroke() {
        foregroundContentDrawable.setStroke(strokeWidth.toFloat(), strokeColorStateList)
    }

    /**
     * Apply content padding to the intermediate contentLayout. Padding includes the user-specified
     * content padding as well as any padding ot prevent corner overlap. The padding is applied to the
     * intermediate contentLayout so that the bounds of the contentLayout match the bounds of the
     * stroke (or card bounds if there is no stroke). This ensures that clipping is applied properly
     * to the inside of the stroke, not around the content.
     */
    fun updateContentPadding() {
        val includeCornerPadding = (shouldAddCornerPaddingInsideCardBackground()
                || shouldAddCornerPaddingOutsideCardBackground())
        // The amount with which to adjust the user provided content padding to account for stroke and
        // shape corners.
        val contentPaddingOffset = ((if (includeCornerPadding) calculateActualCornerPadding() else 0).toInt()
                - parentCardViewCalculatedCornerPadding) as Int
        materialCardView.setAncestorContentPadding(
                userContentPadding.left + contentPaddingOffset,
                userContentPadding.top + contentPaddingOffset,
                userContentPadding.right + contentPaddingOffset,
                userContentPadding.bottom + contentPaddingOffset)
    }

    @RequiresApi(VERSION_CODES.LOLLIPOP)
    fun setRippleColor(rippleColor: ColorStateList?) {
        this.rippleColor = rippleColor
        updateRippleColor()
    }

    fun setCheckedIconTint(checkedIconTint: ColorStateList?) {
        this.checkedIconTint = checkedIconTint
        if (checkedIcon != null) {
            DrawableCompat.setTintList(checkedIcon!!, checkedIconTint)
        }
    }

    fun getCheckedIconTint(): ColorStateList? {
        return checkedIconTint
    }

    fun getRippleColor(): ColorStateList? {
        return rippleColor
    }

    fun getCheckedIcon(): Drawable? {
        return checkedIcon
    }

    fun setCheckedIcon(checkedIcon: Drawable?) {
        this.checkedIcon = checkedIcon
        if (checkedIcon != null) {
            this.checkedIcon = DrawableCompat.wrap(checkedIcon.mutate())
            DrawableCompat.setTintList(checkedIcon, checkedIconTint)
        }
        if (clickableForegroundDrawable != null) {
            val checkedLayer = createCheckedIconLayer()
            clickableForegroundDrawable!!.setDrawableByLayerId(
                    R.id.mtrl_card_checked_layer_id, checkedLayer)
        }
    }

    fun onMeasure(measuredWidth: Int, measuredHeight: Int) {
        if (clickableForegroundDrawable != null) {
            var left = measuredWidth - checkedIconMargin - checkedIconSize
            var bottom = measuredHeight - checkedIconMargin - checkedIconSize
            val isPreLollipop = VERSION.SDK_INT < VERSION_CODES.LOLLIPOP
            if (isPreLollipop || materialCardView.useCompatPadding) {
                bottom -= Math.ceil((2f * calculateVerticalBackgroundPadding()).toDouble()).toInt()
                left -= Math.ceil((2f * calculateHorizontalBackgroundPadding()).toDouble()).toInt()
            }
            var right = checkedIconMargin
            if (ViewCompat.getLayoutDirection(materialCardView) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                // swap left and right
                val tmp = right
                right = left
                left = tmp
            }
            clickableForegroundDrawable!!.setLayerInset(
                    CHECKED_ICON_LAYER_INDEX, left, checkedIconMargin /* top */, right, bottom)
        }
    }

    @RequiresApi(api = VERSION_CODES.M)
    fun forceRippleRedraw() {
        if (rippleDrawable != null) {
            val bounds = rippleDrawable!!.bounds
            // Change the bounds slightly to force the layer to change color, then change the layer again.
            // In API 28 the color for the Ripple is snapshot at the beginning of the animation,
            // it doesn't update when the drawable changes to android:state_checked.
            val bottom = bounds.bottom
            rippleDrawable!!.setBounds(bounds.left, bounds.top, bounds.right, bottom - 1)
            rippleDrawable!!.setBounds(bounds.left, bounds.top, bounds.right, bottom)
        }
    }

    @SuppressLint("RestrictedApi")
    fun setShapeAppearanceModel(shapeAppearanceModel: ShapeAppearanceModel) {
        this.shapeAppearanceModel = shapeAppearanceModel
        background.shapeAppearanceModel = shapeAppearanceModel
        background.setShadowBitmapDrawingEnable(!background.isRoundRect)
        if (foregroundContentDrawable != null) {
            foregroundContentDrawable.shapeAppearanceModel = shapeAppearanceModel
        }
        if (foregroundShapeDrawable != null) {
            foregroundShapeDrawable!!.shapeAppearanceModel = shapeAppearanceModel
        }
        if (compatRippleDrawable != null) {
            compatRippleDrawable!!.shapeAppearanceModel = shapeAppearanceModel
        }
    }

    /**
     * Attempts to update the [InsetDrawable] foreground to use the given [Drawable].
     * Changing the Drawable is only available in M+, so earlier versions will create a new
     * InsetDrawable.
     */
    private fun updateInsetForeground(insetForeground: Drawable) {
        if (VERSION.SDK_INT >= VERSION_CODES.M
                && materialCardView.foreground is InsetDrawable) {
            (materialCardView.foreground as InsetDrawable).drawable = insetForeground
        } else {
            materialCardView.foreground = insetDrawable(insetForeground)
        }
    }

    /**
     * Returns a [Drawable] that insets the given drawable by the amount of padding CardView
     * would add for the shadow. This will always use an [InsetDrawable] even if there is no
     * inset.
     *
     *
     * Always use an InsetDrawable even when the insets are 0 instead of only wrapping in an
     * InsetDrawable when there is an inset. Replacing the background (or foreground) of a [ ] with the same Drawable wrapped into an InsetDrawable will result in the View clearing the
     * original Drawable's callback which should refer to the InsetDrawable.
     */
    private fun insetDrawable(originalDrawable: Drawable?): Drawable {
        var insetVertical = 0
        var insetHorizontal = 0
        val isPreLollipop = VERSION.SDK_INT < VERSION_CODES.LOLLIPOP
        if (isPreLollipop || materialCardView.useCompatPadding) {
            // Calculate the shadow padding used by CardView
            insetVertical = Math.ceil(calculateVerticalBackgroundPadding().toDouble()).toInt()
            insetHorizontal = Math.ceil(calculateHorizontalBackgroundPadding().toDouble()).toInt()
        }
        return object : InsetDrawable(
                originalDrawable, insetHorizontal, insetVertical, insetHorizontal, insetVertical) {
            override fun getPadding(padding: Rect): Boolean {
                // Our very own special InsetDrawable that pretends it does not have padding so that
                // using it as the background will *not* change the padding of the view.
                return false
            }

            /** Don't force the card to be as big as this drawable  */
            override fun getMinimumWidth(): Int {
                return -1
            }

            /** Don't force the card to be as big as this drawable  */
            override fun getMinimumHeight(): Int {
                return -1
            }
        }
    }

    /**
     * Calculates the amount of padding that should be added above and below the background shape.
     * This should only be called pre-lollipop or when using compat padding. This accounts for shadow
     * and corner padding when they are added outside the background.
     */
    private fun calculateVerticalBackgroundPadding(): Float {
        var x =(if (shouldAddCornerPaddingOutsideCardBackground()) calculateActualCornerPadding() else 0).toFloat()
        return (materialCardView.maxCardElevation * CARD_VIEW_SHADOW_MULTIPLIER
                + x)
    }

    /**
     * Calculates the amount of padding that should be added to the left and right of the background
     * shape. This should only be called pre-lollipop or when using compat padding. This accounts for
     * shadow and corner padding when they are added outside the background.
     */
    private fun calculateHorizontalBackgroundPadding(): Float {
        var x =if (shouldAddCornerPaddingOutsideCardBackground()) calculateActualCornerPadding() else 0
        return (materialCardView.maxCardElevation
                + x.toFloat())
    }

    @SuppressLint("RestrictedApi")
    private fun canClipToOutline(): Boolean {
        return VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && background.isRoundRect
    }

    private val parentCardViewCalculatedCornerPadding: Float
        private get() = if (materialCardView.preventCornerOverlap
                && (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP || materialCardView.useCompatPadding)) {
            ((1 - COS_45) * materialCardView.cardViewRadius).toFloat()
        } else 0f

    private fun shouldAddCornerPaddingInsideCardBackground(): Boolean {
        return materialCardView.preventCornerOverlap && !canClipToOutline()
    }

    private fun shouldAddCornerPaddingOutsideCardBackground(): Boolean {
        return (materialCardView.preventCornerOverlap
                && canClipToOutline()
                && materialCardView.useCompatPadding)
    }

    /**
     * Calculates the amount of padding required between the card background shape and the card
     * content such that the entire content is within the bounds of the card background shape.
     *
     *
     * This should only be called when either [ ][.shouldAddCornerPaddingOutsideCardBackground] or [ ][.shouldAddCornerPaddingInsideCardBackground] returns true.
     */
    private fun calculateActualCornerPadding(): Float {
        return Math.max(
                Math.max(
                        calculateCornerPaddingForCornerTreatment(
                                shapeAppearanceModel!!.topLeftCorner, background.topLeftCornerResolvedSize),
                        calculateCornerPaddingForCornerTreatment(
                                shapeAppearanceModel!!.topRightCorner,
                                background.topRightCornerResolvedSize)),
                Math.max(
                        calculateCornerPaddingForCornerTreatment(
                                shapeAppearanceModel!!.bottomRightCorner,
                                background.bottomRightCornerResolvedSize),
                        calculateCornerPaddingForCornerTreatment(
                                shapeAppearanceModel!!.bottomLeftCorner,
                                background.bottomLeftCornerResolvedSize)))
    }

    private fun calculateCornerPaddingForCornerTreatment(treatment: CornerTreatment, size: Float): Float {
        if (treatment is RoundedCornerTreatment) {
            return ((1 - COS_45) * size).toFloat()
        } else if (treatment is CutCornerTreatment) {
            return size / 2
        }
        return 0F
    }

    private val clickableForeground: Drawable
        @RequiresApi(VERSION_CODES.LOLLIPOP) private get() {
            if (rippleDrawable == null) {
                rippleDrawable = createForegroundRippleDrawable()
            }
            if (clickableForegroundDrawable == null) {
                val checkedLayer = createCheckedIconLayer()
                clickableForegroundDrawable = LayerDrawable(arrayOf(rippleDrawable!!, foregroundContentDrawable, checkedLayer))
                clickableForegroundDrawable!!.setId(CHECKED_ICON_LAYER_INDEX, R.id.mtrl_card_checked_layer_id)
            }
            return clickableForegroundDrawable!!
        }

    @RequiresApi(VERSION_CODES.LOLLIPOP)
    private fun createForegroundRippleDrawable(): Drawable {
        if (RippleUtils.USE_FRAMEWORK_RIPPLE) {
            foregroundShapeDrawable = createForegroundShapeDrawable()
            return RippleDrawable(rippleColor!!, null, foregroundShapeDrawable)
        }
        return createCompatRippleDrawable()
    }

    private fun createCompatRippleDrawable(): Drawable {
        val rippleDrawable = StateListDrawable()
        compatRippleDrawable = createForegroundShapeDrawable()
        compatRippleDrawable!!.fillColor = rippleColor
        rippleDrawable.addState(intArrayOf(android.R.attr.state_pressed), compatRippleDrawable)
        return rippleDrawable
    }

    @RequiresApi(VERSION_CODES.LOLLIPOP)
    private fun updateRippleColor() {
        if (RippleUtils.USE_FRAMEWORK_RIPPLE && rippleDrawable != null) {
            (rippleDrawable as RippleDrawable).setColor(rippleColor)
        } else if (compatRippleDrawable != null) {
            compatRippleDrawable!!.fillColor = rippleColor
        }
    }

    private fun createCheckedIconLayer(): Drawable {
        val checkedLayer = StateListDrawable()
        if (checkedIcon != null) {
            checkedLayer.addState(CHECKED_STATE_SET, checkedIcon)
        }
        return checkedLayer
    }

    private fun createForegroundShapeDrawable(): MaterialShapeDrawable {
        return MaterialShapeDrawable(shapeAppearanceModel!!)
    }

    companion object {
        private val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)
        private const val DEFAULT_STROKE_VALUE = -1

        // used to calculate content padding
        private val COS_45 = Math.cos(Math.toRadians(45.0))

        /**
         * Multiplier for [MaterialCardView.getMaxCardElevation] to calculate vertical shadow
         * padding. Horizontal shadow padding is equal to getMaxCardElevation(). Shadow padding is the
         * padding around the visible card that [CardView] adds in order to have space to render
         * shadows pre-Lollipop.
         *
         *
         * CardView's pre-Lollipop shadow is getMaxCardElevation() larger than the card on all sides
         * and offset down by 0.5 x getMaxCardElevation(). Thus, the additional padding required is:
         *
         *
         *  * Left & Right: getMaxCardElevation()
         *  * Top: 0.5 x getMaxCardElevation()
         *  * Bottom: 1.5 x getMaxCardElevation()
         *
         *
         *
         * In order to keep content that is centered in the center, extra padding is added on top to
         * match the necessary bottom padding.
         */
        private const val CARD_VIEW_SHADOW_MULTIPLIER = 1.5f
        private const val CHECKED_ICON_LAYER_INDEX = 2
    }

    init {
        background = MaterialShapeDrawable(materialCardView.context, attrs, defStyleAttr, defStyleRes)
        background.initializeElevationOverlay(materialCardView.context)
        background.setShadowColor(Color.DKGRAY)
        val shapeAppearanceModelBuilder = background.shapeAppearanceModel.toBuilder()
        val cardViewAttributes = materialCardView.context
                .obtainStyledAttributes(attrs, R.styleable.CardView, defStyleAttr, R.style.CardView)
        if (cardViewAttributes.hasValue(R.styleable.CardView_cardCornerRadius)) {
            // If cardCornerRadius is set, let it override the shape appearance.
            shapeAppearanceModelBuilder.setAllCornerSizes(
                    cardViewAttributes.getDimension(R.styleable.CardView_cardCornerRadius, 0f))
        }
        foregroundContentDrawable = MaterialShapeDrawable()
        setShapeAppearanceModel(shapeAppearanceModelBuilder.build())
        cardViewAttributes.recycle()
    }
}

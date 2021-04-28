package com.swiggy.lib.card

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.AttributeSet
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Checkable
import androidx.annotation.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import com.google.android.material.internal.ThemeEnforcement
import com.google.android.material.shape.MaterialShapeUtils
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.Shapeable
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import com.swiggy.lib.R


@RequiresApi(VERSION_CODES.LOLLIPOP)
@SuppressLint("RestrictedApi")
open class card @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null /* attrs */, defStyleAttr: Int = R.attr.materialCardViewStyle) : CardView(MaterialThemeOverlay.wrap(context!!, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr), Checkable, Shapeable {
    /** Interface definition for a callback to be invoked when the card checked state changes.  */
    interface OnCheckedChangeListener {
        /**
         * Called when the checked state of a compound button has changed.
         *
         * @param card The Material Card View whose state has changed.
         * @param isChecked The new checked state of MaterialCardView.
         */
        fun onCheckedChanged(card: card?, isChecked: Boolean)
    }

    private val cardViewHelper: cardViewUtil

    /**
     * Keep track of when [CardView] is done initializing because we don't want to use the
     * [Drawable] that it passes to [.setBackground].
     */
    private val isParentCardViewDoneInitializing: Boolean
    private var checked = false

    /**
     * Call this when the Card is being dragged to apply the right color and elevation changes.
     *
     * @param dragged whether the card is currently being dragged or at rest.
     */
    var isDragged = false
        set(dragged) {
            if (isDragged != dragged) {
                field = dragged
                refreshDrawableState()
                forceRippleRedrawIfNeeded()
                invalidate()
            }
        }
    private var onCheckedChangeListener: OnCheckedChangeListener? = null
    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = ACCESSIBILITY_CLASS_NAME
        info.isCheckable = isCheckable
        info.isClickable = isClickable
        info.isChecked = isChecked
    }

    override fun onInitializeAccessibilityEvent(accessibilityEvent: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent)
        accessibilityEvent.className = ACCESSIBILITY_CLASS_NAME
        accessibilityEvent.isChecked = isChecked
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        cardViewHelper.onMeasure(measuredWidth, measuredHeight)
    }

    /**
     * Sets the stroke color of this card view.
     *
     * @param strokeColor The ColorStateList of the stroke.
     */
    fun setStrokeColor(strokeColor: ColorStateList?) {
        if (strokeColor != null) {
            cardViewHelper.setStrokeColor(strokeColor)
        }
    }

    /**
     * Sets the stroke color of this card view.
     *
     * @param strokeColor The color of the stroke.
     */
    @get:Deprecated("use {@link #getStrokeColorStateList()} ")
    @get:ColorInt
    var strokeColor: Int
        get() = cardViewHelper.getStrokeColor()
        set(strokeColor) {
            cardViewHelper.setStrokeColor(ColorStateList.valueOf(strokeColor))
        }

    /** Returns the stroke ColorStateList of this card view.  */
    val strokeColorStateList: ColorStateList?
        get() = cardViewHelper.strokeColorStateList
    /** Returns the stroke width of this card view.  */
    /**
     * Sets the stroke width of this card view.
     *
     * @param strokeWidth The width in pixels of the stroke.
     */
    @get:Dimension
    var strokeWidth: Int
        get() = cardViewHelper.getStrokeWidth()
        set(strokeWidth) {
            cardViewHelper.setStrokeWidth(strokeWidth)
        }

    override fun setRadius(radius: Float) {
        super.setRadius(radius)
        cardViewHelper.cornerRadius=(radius)
    }

    override fun getRadius(): Float {
        return cardViewHelper.cornerRadius
    }

    val cardViewRadius: Float
        get() = super@card.getRadius()
    /**
     * Returns the interpolation on the Shape Path of the card.
     * @see MaterialShapeDrawable.getInterpolation
     * @see ShapeAppearanceModel
     */
    /**
     * Sets the interpolation on the Shape Path of the card. Useful for animations.
     * @see MaterialShapeDrawable.setInterpolation
     * @see ShapeAppearanceModel
     */
    @get:FloatRange(from = 0.0, to = 1.0)
    var progress: Float
        get() = cardViewHelper.progress
        set(progress) {
            cardViewHelper.progress=(progress)
        }

    override fun setContentPadding(left: Int, top: Int, right: Int, bottom: Int) {
        cardViewHelper.setUserContentPadding(left, top, right, bottom)
    }

    fun setAncestorContentPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setContentPadding(left, top, right, bottom)
    }

    override fun getContentPaddingLeft(): Int {
        return cardViewHelper.userContentPadding.left
    }

    override fun getContentPaddingTop(): Int {
        return cardViewHelper.userContentPadding.top
    }

    override fun getContentPaddingRight(): Int {
        return cardViewHelper.userContentPadding.right
    }

    override fun getContentPaddingBottom(): Int {
        return cardViewHelper.userContentPadding.bottom
    }

    override fun setCardBackgroundColor(@ColorInt color: Int) {
        cardViewHelper.cardBackgroundColor=(ColorStateList.valueOf(color))
    }

    override fun setCardBackgroundColor(color: ColorStateList?) {
        cardViewHelper.cardBackgroundColor=(color)
    }

    override fun getCardBackgroundColor(): ColorStateList {
        return cardViewHelper.cardBackgroundColor!!
    }
    /**
     * Sets the ripple color for this card.
     *
     * @attr ref com.google.android.material.R.styleable#MaterialCardView_cardForegroundColor
     * @see .setCardForegroundColor
     */
    /**
     * Sets the foreground color for this card.
     *
     * @param foregroundColor Color to use for the foreground.
     * @attr ref com.google.android.material.R.styleable#MaterialCardView_cardForegroundColor
     * @see .getCardForegroundColor
     */
    var cardForegroundColor: ColorStateList?
        get() = cardViewHelper.cardForegroundColor
        set(foregroundColor) {
            cardViewHelper.cardForegroundColor=(foregroundColor)
        }

    override fun setClickable(clickable: Boolean) {
        super.setClickable(clickable)
        cardViewHelper?.updateClickable()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        MaterialShapeUtils.setParentAbsoluteElevation(this, cardViewHelper.background)
    }

    override fun setCardElevation(elevation: Float) {
        super.setCardElevation(elevation)
        cardViewHelper.updateElevation()
    }

    override fun setMaxCardElevation(maxCardElevation: Float) {
        super.setMaxCardElevation(maxCardElevation)
        cardViewHelper.updateInsets()
    }

    override fun setUseCompatPadding(useCompatPadding: Boolean) {
        super.setUseCompatPadding(useCompatPadding)
        cardViewHelper.updateInsets()
        cardViewHelper.updateContentPadding()
    }

    override fun setPreventCornerOverlap(preventCornerOverlap: Boolean) {
        super.setPreventCornerOverlap(preventCornerOverlap)
        cardViewHelper.updateInsets()
        cardViewHelper.updateContentPadding()
    }

    override fun setBackground(drawable: Drawable) {
        setBackgroundDrawable(drawable)
    }

    override fun setBackgroundDrawable(drawable: Drawable) {
        if (isParentCardViewDoneInitializing) {
            if (!cardViewHelper.isBackgroundOverwritten) {
                Log.i(LOG_TAG, "Setting a custom background is not supported.")
                cardViewHelper.isBackgroundOverwritten=(true)
            }
            super.setBackgroundDrawable(drawable)
        }
        // Do nothing if CardView isn't done initializing because we don't want to use its background.
    }

    /** Allows [MaterialCardViewHelper] to set the background.  */
    fun setBackgroundInternal(drawable: Drawable?) {
        super.setBackgroundDrawable(drawable)
    }

    override fun isChecked(): Boolean {
        return checked
    }

    override fun setChecked(checked: Boolean) {
        if (this.checked != checked) {
            toggle()
        }
    }
    /**
     * Returns whether this Card is checkable.
     *
     * @see .setCheckable
     * @attr ref com.google.android.material.R.styleable#MaterialCardView_android_checkable
     */
    /**
     * Sets whether this Card is checkable.
     *
     * @param checkable Whether this chip is checkable.
     * @attr ref com.google.android.material.R.styleable#MaterialCardView_android_checkable
     */
    var isCheckable: Boolean
        get() = cardViewHelper != null && cardViewHelper.isCheckable
        set(checkable) {
            cardViewHelper.isCheckable=(checkable)
        }

    override fun toggle() {
        if (isCheckable && isEnabled) {
            checked = !checked
            refreshDrawableState()
            forceRippleRedrawIfNeeded()
            if (onCheckedChangeListener != null) {
                onCheckedChangeListener!!.onCheckedChanged(this, checked)
            }
        }
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 3)
        if (isCheckable) {
            mergeDrawableStates(drawableState, CHECKABLE_STATE_SET)
        }
        if (isChecked) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET)
        }
        if (isDragged) {
            mergeDrawableStates(drawableState, DRAGGED_STATE_SET)
        }
        return drawableState
    }

    /**
     * Register a callback to be invoked when the checked state of this Card changes.
     *
     * @param listener the callback to call on checked state change
     */
    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        onCheckedChangeListener = listener
    }

    /**
     * Sets the ripple color resource for this card.
     *
     * @param rippleColorResourceId Color resource to use for the ripple.
     * @attr ref com.google.android.material.R.styleable#MaterialCardView_rippleColor
     * @see .setRippleColor
     * @see .getRippleColor
     */
    fun setRippleColorResource(@ColorRes rippleColorResourceId: Int) {
        cardViewHelper.setRippleColor(
                AppCompatResources.getColorStateList(context, rippleColorResourceId))
    }
    /**
     * Gets the ripple color for this card.
     *
     * @return The color used for the ripple.
     * @attr ref com.google.android.material.R.styleable#MaterialCardView_rippleColor
     * @see .setRippleColor
     * @see .setRippleColorResource
     */
    /**
     * Sets the ripple color for this card.
     *
     * @param rippleColor Color to use for the ripple.
     * @attr ref com.google.android.material.R.styleable#MaterialCardView_rippleColor
     * @see .setRippleColorResource
     * @see .getRippleColor
     */
    var rippleColor: ColorStateList?
        get() = cardViewHelper.getRippleColor()
        set(rippleColor) {
            cardViewHelper.setRippleColor(rippleColor)
        }
    /**
     * Returns this cards's checked icon.
     *
     * @see .setCheckedIcon
     * @attr ref com.google.android.material.R.styleable#MaterialCardView_checkedIcon
     */
    /**
     * Sets this card's checked icon.
     *
     * @param checkedIcon This card's checked icon.
     * @attr ref com.google.android.material.R.styleable#MaterialCardView_checkedIcon
     */
    var checkedIcon: Drawable?
        get() = cardViewHelper.getCheckedIcon()
        set(checkedIcon) {
            cardViewHelper.setCheckedIcon(checkedIcon)
        }

    /**
     * Sets this card's checked icon using a resource id.
     *
     * @param id The resource id of this Card's checked icon.
     * @attr ref com.google.android.material.R.styleable#MaterialCardView_checkedIcon
     */
    fun setCheckedIconResource(@DrawableRes id: Int) {
        cardViewHelper.setCheckedIcon(AppCompatResources.getDrawable(context, id))
    }
    /**
     * Returns the [android.content.res.ColorStateList] used to tint the checked icon.
     *
     * @see .setCheckedIconTint
     * @attr ref com.google.android.material.R.styleable#MaterialCardView_checkedIconTint
     */
    /**
     * Sets this checked icon color tint using the specified [ ].
     *
     * @param checkedIconTint The tint color of this chip's icon.
     * @attr ref com.google.android.material.R.styleable#MaterialCardView_checkedIconTint
     */
    var checkedIconTint: ColorStateList?
        get() = cardViewHelper.getCheckedIconTint()
        set(checkedIconTint) {
            cardViewHelper.setCheckedIconTint(checkedIconTint)
        }

    /**
     * Sets the size of the checked icon
     *
     * @param checkedIconSize checked icon size
     * @attr ref com.google.android.material.R.styleable#MaterialCardView_checkedIconSize
     */
    @get:Dimension
    var checkedIconSize: Int
        get() = cardViewHelper.checkedIconSize
        set(checkedIconSize) {
            cardViewHelper.checkedIconSize=(checkedIconSize)
        }

    /**
     * Sets the size of the checked icon using a resource id.
     *
     * @param checkedIconSizeResId The resource id of this Card's checked icon size
     * @attr ref com.google.android.material.R.styleable#MaterialCardView_checkedIconSize
     */
    fun setCheckedIconSizeResource(@DimenRes checkedIconSizeResId: Int) {
        if (checkedIconSizeResId != 0) {
            cardViewHelper.checkedIconSize=(resources.getDimensionPixelSize(checkedIconSizeResId))
        }
    }

    @get:Dimension
    var checkedIconMargin: Int
        get() = cardViewHelper.checkedIconMargin
        set(checkedIconMargin) {
            cardViewHelper.checkedIconMargin=(checkedIconMargin)
        }

    /**
     * Sets the margin of the checked icon using a resource id.
     *
     * @param checkedIconMarginResId The resource id of this Card's checked icon margin
     * @attr ref com.google.android.material.R.styleable#MaterialCardView_checkedIconMargin
     */
    fun setCheckedIconMarginResource(@DimenRes checkedIconMarginResId: Int) {
        if (checkedIconMarginResId != NO_ID) {
            cardViewHelper.checkedIconMargin=(
                    resources.getDimensionPixelSize(checkedIconMarginResId))
        }
    }

    private val boundsAsRectF: RectF
        private get() {
            val boundsRectF = RectF()
            boundsRectF.set(cardViewHelper.background.bounds)
            return boundsRectF
        }

    @SuppressLint("RestrictedApi")
    override fun setShapeAppearanceModel(shapeAppearanceModel: ShapeAppearanceModel) {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            clipToOutline = shapeAppearanceModel.isRoundRect(boundsAsRectF)
        }
        cardViewHelper.setShapeAppearanceModel(shapeAppearanceModel)
    }

    /**
     * Due to limitations in the current implementation, if you modify the returned object
     * call [.setShapeAppearanceModel] again with the modified value
     * to propagate the required changes.
     */
    override fun getShapeAppearanceModel(): ShapeAppearanceModel {
        return cardViewHelper.shapeAppearanceModel!!
    }

    private fun forceRippleRedrawIfNeeded() {
        if (VERSION.SDK_INT > VERSION_CODES.O) {
            cardViewHelper.forceRippleRedraw()
        }
    }

    companion object {
        private val CHECKABLE_STATE_SET = intArrayOf(android.R.attr.state_checkable)
        private val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)
        private val DRAGGED_STATE_SET = intArrayOf(R.attr.state_dragged)
        private val DEF_STYLE_RES = R.style.Widget_MaterialComponents_CardView
        private const val LOG_TAG = "MaterialCardView"
        private const val ACCESSIBILITY_CLASS_NAME = "androidx.cardview.widget.CardView"
    }

    init {
        var context = context
        isParentCardViewDoneInitializing = true
        // Ensure we are using the correctly themed context rather than the context that was passed in.
        context = getContext()
        val attributes = ThemeEnforcement.obtainStyledAttributes(
                context, attrs, R.styleable.MaterialCardView, defStyleAttr, DEF_STYLE_RES
        )

        // Loads and sets background drawable attributes.
        cardViewHelper = cardViewUtil(this, attrs, defStyleAttr, DEF_STYLE_RES)
        cardViewHelper.cardBackgroundColor=(super.getCardBackgroundColor())
        cardViewHelper.setUserContentPadding(
                super.getContentPaddingLeft(),
                super.getContentPaddingTop(),
                super.getContentPaddingRight(),
                super.getContentPaddingBottom())
        // Zero out the AppCompat CardView's content padding, the padding will be added to the internal
        // contentLayout.
        cardViewHelper.loadFromAttributes(attributes)
        attributes.recycle()
    }
}

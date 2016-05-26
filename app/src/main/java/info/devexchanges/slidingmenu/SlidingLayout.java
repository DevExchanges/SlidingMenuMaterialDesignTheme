package info.devexchanges.slidingmenu;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;

public class SlidingLayout extends LinearLayout {

    // Duration of sliding animation, in miliseconds
    private static final int SLIDING_DURATION = 500;

    // Query Scroller every 16 miliseconds
    private static final int QUERY_INTERVAL = 16;

    // Sliding width
    int sldingLayoutWidth;

    // Sliding menu
    private View menu;

    // Main content
    private View content;

    // menu does not occupy some right space
    // This should be updated correctly later in onMeasure
    private static int menuRightMargin = 0;

    // The state of menu
    private enum MenuState {
        HIDING,
        HIDDEN,
        SHOWING,
        SHOWN,
    }

    // content will be layouted based on this X offset
    // Normally, contentXOffset = menu.getLayoutParams().width = this.getWidth - menuRightMargin
    private int contentXOffset;

    // menu is hidden when initializing
    private MenuState currentMenuState = MenuState.HIDDEN;

    // Scroller is used to facilitate animation
    private Scroller menuScroller = new Scroller(this.getContext(),
            new EaseInInterpolator());

    // Used to query Scroller about scrolling position
    // Note: The 3rd paramter to startScroll is the distance
    private Runnable menuRunnable = new MenuRunnable();
    private Handler menuHandler = new Handler();

    // Previous touch position
    int prevX = 0;

    // Is user dragging the content
    boolean isDragging = false;

    // Used to facilitate ACTION_UP 
    int lastDiffX = 0;

    public SlidingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlidingLayout(Context context) {
        super(context);
    }

    // Overriding LinearLayout core methods
    // Ask all children to measure themselves and compute the measurement of this
    // layout based on the children
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        sldingLayoutWidth = MeasureSpec.getSize(widthMeasureSpec);
        //Sliding menu will take 85% screen size when completely shown
        menuRightMargin = sldingLayoutWidth * 15 / 100;
    }

    // This is called when SlidingLayout is attached to window
    // At this point it has a Surface and will start drawing. 
    // Note that this function is guaranteed to be called before onDraw
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Get our 2 children views
        menu = this.getChildAt(0);
        content = this.getChildAt(1);

        // Attach View.OnTouchListener
        content.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return onContentTouch(v, event);
            }
        });

        menu.setVisibility(View.GONE);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // True if SlidingLayout's size and position has changed
        // If true, calculate child views size
        if (changed) {
            // Note: LayoutParams are used by views to tell their parents how they want to be laid out
            // content View occupies the full height and width
            LayoutParams contentLayoutParams = (LayoutParams) content.getLayoutParams();
            contentLayoutParams.height = this.getHeight();
            contentLayoutParams.width = this.getWidth();

            // menu View occupies the full height, but certain width
            LayoutParams menuLayoutParams = (LayoutParams) menu.getLayoutParams();
            menuLayoutParams.height = this.getHeight();
            menuLayoutParams.width = this.getWidth() - menuRightMargin;
        }

        // Layout the child views    
        menu.layout(left, top, right - menuRightMargin, bottom);
        content.layout(left + contentXOffset, top, right + contentXOffset, bottom);
    }

    // Custom methods for SlidingLayout
    // Used to show/hide menu accordingly
    public void toggleMenu() {
        // Do nothing if sliding is in progress
        if (currentMenuState == MenuState.HIDING || currentMenuState == MenuState.SHOWING)
            return;

        switch (currentMenuState) {
            case HIDDEN:
                currentMenuState = MenuState.SHOWING;
                menu.setVisibility(View.VISIBLE);
                menuScroller.startScroll(0, 0, menu.getLayoutParams().width,
                        0, SLIDING_DURATION);
                break;
            case SHOWN:
                currentMenuState = MenuState.HIDING;
                menuScroller.startScroll(contentXOffset, 0, -contentXOffset,
                        0, SLIDING_DURATION);
                break;
            default:
                break;
        }

        // Begin querying
        menuHandler.postDelayed(menuRunnable, QUERY_INTERVAL);

        // Invalite this whole SlidingLayout, causing onLayout() to be called
        this.invalidate();
    }

    // Query Scroller
    protected class MenuRunnable implements Runnable {
        @Override
        public void run() {
            boolean isScrolling = menuScroller.computeScrollOffset();
            adjustContentPosition(isScrolling);
        }
    }

    // Adjust content View position to match sliding animation
    private void adjustContentPosition(boolean isScrolling) {
        int scrollerXOffset = menuScroller.getCurrX();

        // Translate content View accordingly
        content.offsetLeftAndRight(scrollerXOffset - contentXOffset);

        contentXOffset = scrollerXOffset;

        // Invalite this whole Slidinglayout, causing onLayout() to be called
        this.invalidate();

        // Check if animation is in progress
        if (isScrolling)
            menuHandler.postDelayed(menuRunnable, QUERY_INTERVAL);
        else
            this.onMenuSlidingComplete();
    }

    // Called when sliding is complete
    private void onMenuSlidingComplete() {
        switch (currentMenuState) {
            case SHOWING:
                currentMenuState = MenuState.SHOWN;
                break;
            case HIDING:
                currentMenuState = MenuState.HIDDEN;
                menu.setVisibility(View.GONE);
                break;
            default:
                return;
        }
    }

    // Make scrolling more natural. Move more quickly at the end
    protected class EaseInInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float t) {
            return (float) Math.pow(t - 1, 5) + 1;
        }

    }

    // Is menu completely shown
    public boolean isMenuShown() {
        return currentMenuState == MenuState.SHOWN;
    }

    // Handle touch event on content View
    public boolean onContentTouch(View v, MotionEvent event) {
        // Do nothing if sliding is in progress
        if (currentMenuState == MenuState.HIDING || currentMenuState == MenuState.SHOWING)
            return false;

        // getRawX returns X touch point corresponding to screen
        // getX sometimes returns screen X, sometimes returns content View X
        int curX = (int) event.getRawX();
        int diffX = 0;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                prevX = curX;
                return true;

            case MotionEvent.ACTION_MOVE:
                // Set menu to Visible when user start dragging the content View
                if (!isDragging) {
                    isDragging = true;
                    menu.setVisibility(View.VISIBLE);
                }

                // How far we have moved since the last position
                diffX = curX - prevX;

                // Prevent user from dragging beyond border
                if (contentXOffset + diffX <= 0) {
                    // Don't allow dragging beyond left border
                    // Use diffX will make content cross the border, so only translate by -contentXOffset
                    diffX = -contentXOffset;
                } else if (contentXOffset + diffX > sldingLayoutWidth - menuRightMargin) {
                    // Don't allow dragging beyond menu width
                    diffX = sldingLayoutWidth - menuRightMargin - contentXOffset;
                }

                // Translate content View accordingly
                content.offsetLeftAndRight(diffX);

                contentXOffset += diffX;

                // Invalite this whole Slidinglayout, causing onLayout() to be called
                this.invalidate();

                prevX = curX;
                lastDiffX = diffX;
                return true;

            case MotionEvent.ACTION_UP:
                // Start scrolling
                // Remember that when content has a chance to cross left border, lastDiffX is set to 0
                if (lastDiffX > 0) {
                    // User wants to show menu
                    currentMenuState = MenuState.SHOWING;

                    // Start scrolling from contentXOffset
                    menuScroller.startScroll(contentXOffset, 0, menu.getLayoutParams().width - contentXOffset,
                            0, SLIDING_DURATION);
                } else if (lastDiffX < 0) {
                    // User wants to hide menu
                    currentMenuState = MenuState.HIDING;
                    menuScroller.startScroll(contentXOffset, 0, -contentXOffset,
                            0, SLIDING_DURATION);
                }

                // Begin querying
                menuHandler.postDelayed(menuRunnable, QUERY_INTERVAL);

                // Invalite this whole Slidinglayout, causing onLayout() to be called
                this.invalidate();

                // Done dragging
                isDragging = false;
                prevX = 0;
                lastDiffX = 0;
                return true;

            default:
                break;
        }
        return false;
    }
}
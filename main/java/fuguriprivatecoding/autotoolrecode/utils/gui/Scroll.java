package fuguriprivatecoding.autotoolrecode.utils.gui;

import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.time.DeltaTracker;
import lombok.Getter;
import lombok.Setter;
import java.awt.*;

@Getter
@Setter
public class Scroll {

    private float scroll;
    private float scrollTotalHeight;
    private float visibleHeight;
    private final float scrollStep;

    EasingAnimation scrollAnim = new EasingAnimation();

    public Scroll(float scrollStep) {
        this.scrollStep = scrollStep;
    }

    public void update(float scrollTotalHeight, float visibleHeight) {
        this.scrollTotalHeight = scrollTotalHeight;
        this.visibleHeight = visibleHeight;
        clampScroll();
    }

    public void handleScrollInput(boolean isHovered) {
        if (isHovered) {
            scroll += (float) DeltaTracker.getDeltaScroll() / 120 * scrollStep;
            clampScroll();
        }
    }

    private void clampScroll() {
        float maxScroll = Math.max(0, scrollTotalHeight - visibleHeight);
        scroll = Math.clamp(scroll, -maxScroll, 0);
        scrollAnim.setEnd(scroll);
    }

    public void setScroll(float scroll) {
        this.scroll = scroll;
        scrollAnim.setValue(scroll);
        clampScroll();
    }

    public void reset() {
        scroll = 0;
    }

    public boolean canScroll() {
        return scrollTotalHeight > visibleHeight;
    }

    public float getScrollPercentage() {
        float maxScroll = Math.max(scrollTotalHeight - visibleHeight, 0);
        return maxScroll > 0 ? -scroll / maxScroll : 0;
    }
}
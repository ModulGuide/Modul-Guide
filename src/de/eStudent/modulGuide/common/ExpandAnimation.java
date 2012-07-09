package de.eStudent.modulGuide.common;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout.LayoutParams;

/**
 * This animation class is animating the expanding and reducing the size of a
 * view. The animation toggles between the Expand and Reduce, depending on the
 * current state of the view
 * 
 * @author Udinic / We (Original geändert) 
 * @link http://www.dewen.org/q/1776/android%E5%A6%82%E4%BD%95%E5%AE%9E%E7%8E%B0%E4%B8%80%E4%B8%AA%E5%8F%AF%E4%BB%A5%E5%B1%95%E5%BC%80%E5%92%8C%E5%8F%A0%E8%B5%B7%E6%AF%8F%E4%B8%80%E9%A1%B9%E7%9A%84listview%EF%BC%9F
 * 
 */
public class ExpandAnimation extends Animation
{
	private View view;
	private LayoutParams mViewLayoutParams;
	private int initialHeight;
	private boolean expand = false;

	/**
	 * Initialize the animation
	 * 
	 * @param view
	 *            The layout we want to animate
	 * @param duration
	 *            The duration of the animation, in ms
	 */
	public ExpandAnimation(View view, int duration)
	{

		setDuration(duration);
		this.view = view;

		mViewLayoutParams = (LayoutParams) view.getLayoutParams();

		// if the bottom margin is 0,
		// then after the animation will end it'll be negative, and invisible.
		expand = view.getVisibility() == View.INVISIBLE;

		// view.measure(LayoutParams.FILL_PARENT ,LayoutParams.WRAP_CONTENT);

		// mMarginStart = mViewLayoutParams.bottomMargin;
		// mMarginEnd = (mMarginStart == 0 ? (0- view.getHeight()) : 0);

	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight)
	{
		// mMarginStart = !expand ? (0 - height) : 0;
		// mMarginEnd = !expand ? 0 : 0 - height;
		initialHeight = height;

		if (expand)
		{
			mViewLayoutParams.bottomMargin = -initialHeight;
			// mViewLayoutParams.height = 0;
			view.requestLayout();
			view.setVisibility(View.VISIBLE);

		}

		super.initialize(width, height, parentWidth, parentHeight);

		Log.d("height", "" + height);

	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t)
	{
		super.applyTransformation(interpolatedTime, t);

		// Calculating the new bottom margin, and setting it
		// mViewLayoutParams.bottomMargin = mMarginStart + (int) ((mMarginEnd -
		// mMarginStart) * interpolatedTime);

		if (expand)
		{
			// mViewLayoutParams.height = (int) (initialHeight *
			// interpolatedTime);
			mViewLayoutParams.bottomMargin = -initialHeight + (int) (initialHeight * interpolatedTime);

		} else
		{
			// mViewLayoutParams.height = (int) (initialHeight * (1 -
			// interpolatedTime));
			mViewLayoutParams.bottomMargin = (int) (-initialHeight * interpolatedTime);

		}

		// Making sure we didn't run the ending before (it happens!)

		if (interpolatedTime == 1 && !expand)
		{
			view.setVisibility(View.INVISIBLE);
			view.getLayoutParams().height = initialHeight;
			mViewLayoutParams.bottomMargin = -initialHeight;
		}

		// Invalidating the layout, making us seeing the changes we made
		view.requestLayout();

	}
}
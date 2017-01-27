package com.magic09.magicfileselector.utils;

import android.app.Activity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionItemTarget;
import com.github.amlcurran.showcaseview.targets.PointTarget;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.magic09.magicfilechooser.R;


/**
 * HelpDisplay provides static methods for displaying and clearing
 * ShowcaseViews.
 * @author dream09
 *
 */
public class HelpDisplay {
	static final String TAG = "HelpDisplay";
	
	/* Variables */
	private static final int FADE_DURATION = 500;
	
	
	
	/* Constructor */
	public HelpDisplay() {}
	
	
	
	/* Methods */
	
	/**
	 * Helper method to display a ShowcaseView for the action item
	 * specified in the argument actionTargetId.
	 * @param activity
	 * @param actionTargetId
	 * @param title
	 * @param text
	 */
	public static ShowcaseView displayActionHelp(Activity activity, int actionTargetId, String title, String text) {
		Target target = new ActionItemTarget(activity, actionTargetId);
		return displayHelp(activity, target, title, text);
	}
	
	/**
	 * Helper method to display a ShowcaseView for the view item
	 * specified in the argument viewTargetId.
	 * @param activity
	 * @param viewTargetId
	 * @param title
	 * @param text
	 */
	public static ShowcaseView displayItemHelp(Activity activity, int viewTargetId, String title, String text) {
		Target target = new ViewTarget(viewTargetId, activity);
		return displayHelp(activity, target, title, text);
	}
	
	/**
	 * Helper method to display a ShowcaseView at the point
	 * specified by the arguments xPos and yPos.
	 * @param activity
	 * @param xPos
	 * @param yPos
	 * @param title
	 * @param text
	 */
	public static ShowcaseView displayPointHelp(Activity activity, int xPos, int yPos, String title, String text) {
		Target target = new PointTarget(xPos, yPos);
		return displayHelp(activity, target, title, text);
	}
	
	/**
	 * Helper method to display a ShowcaseView with no point focus.
	 * @param activity
	 * @param title
	 * @param text
	 */
	public static ShowcaseView displayNoPointHelp(Activity activity, String title, String text) {
		return displayHelp(activity, null, title, text);
	}
	
	/**
	 * Method displays a ShowcaseView for the target specified in
	 * the argument target with the title and text specified the
	 * arguments of the same name.  Fades in.
	 * @param activity
	 * @param target
	 * @param title
	 * @param text
	 */
	private static ShowcaseView displayHelp(Activity activity, Target target, String title, String text) {
		// Setup build based on arguments
		ShowcaseView.Builder showcaseViewBuilder = new ShowcaseView.Builder(activity)
			.setStyle(R.style.MyShowcaseTheme);
		if (target != null)
			showcaseViewBuilder.setTarget(target);
		if (title != null)
			showcaseViewBuilder.setContentTitle(title);
		if (text != null)
			showcaseViewBuilder.setContentText(text);
		
		// Build, start animation in and then return it.
		ShowcaseView showcaseView = showcaseViewBuilder.build();
		showcaseView.setAlpha(0);
		Animation fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setDuration(FADE_DURATION);
		showcaseView.startAnimation(fadeIn);
		return showcaseView;
	}
	
}

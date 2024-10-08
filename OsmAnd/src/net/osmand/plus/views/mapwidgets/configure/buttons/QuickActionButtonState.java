package net.osmand.plus.views.mapwidgets.configure.buttons;

import static net.osmand.plus.quickaction.ButtonAppearanceParams.DEFAULT_ACTION_BUTTON_SIZE;
import static net.osmand.plus.quickaction.ButtonAppearanceParams.DEFAULT_ICON_ID;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.quickaction.ButtonAppearanceParams;
import net.osmand.plus.quickaction.QuickAction;
import net.osmand.plus.settings.backend.ApplicationMode;
import net.osmand.plus.settings.backend.preferences.CommonPreference;
import net.osmand.plus.settings.backend.preferences.FabMarginPreference;
import net.osmand.util.Algorithms;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuickActionButtonState extends MapButtonState {

	public static final String DEFAULT_BUTTON_ID = "quick_actions";

	private final CommonPreference<Boolean> statePref;
	private final CommonPreference<String> namePref;
	private final CommonPreference<String> quickActionsPref;
	private final CommonPreference<String> iconPref;
	private final CommonPreference<Integer> sizePref;
	private final CommonPreference<Float> opacityPref;
	private final CommonPreference<Integer> cornerRadiusPref;
	private final FabMarginPreference fabMarginPref;

	private List<QuickAction> quickActions = new ArrayList<>();

	public QuickActionButtonState(@NonNull OsmandApplication app, @NonNull String id) {
		super(app, id);
		this.statePref = settings.registerBooleanPreference(id + "_state", false).makeProfile();
		this.namePref = settings.registerStringPreference(id + "_name", null).makeGlobal().makeShared();
		this.iconPref = settings.registerStringPreference(id + "_icon", DEFAULT_ICON_ID).makeGlobal().makeShared();
		this.sizePref = settings.registerIntPreference(id + "_size", DEFAULT_ACTION_BUTTON_SIZE).makeGlobal().makeShared();
		this.opacityPref = settings.registerFloatPreference(id + "_opacity", 0.5f).makeGlobal().makeShared();
		this.cornerRadiusPref = settings.registerIntPreference(id + "_corner_radius", 36).makeGlobal().makeShared();
		this.quickActionsPref = settings.registerStringPreference(id + "_list", null).makeGlobal().makeShared().storeLastModifiedTime();
		this.fabMarginPref = new FabMarginPreference(settings, id + "_fab_margin");
	}

	@Override
	public boolean isEnabled() {
		return statePref.get();
	}

	public void setEnabled(boolean enabled) {
		statePref.set(enabled);
	}

	@NonNull
	@Override
	public String getName() {
		String name = namePref.get();
		return Algorithms.isEmpty(name) ? app.getString(R.string.configure_screen_quick_action) : name;
	}

	public boolean hasCustomName() {
		return !Algorithms.isEmpty(namePref.get());
	}

	public void setName(@NonNull String name) {
		namePref.set(name);
	}

	@NonNull
	public List<QuickAction> getQuickActions() {
		return quickActions;
	}

	@Nullable
	public QuickAction getQuickAction(long id) {
		for (QuickAction action : quickActions) {
			if (action.getId() == id) {
				return action;
			}
		}
		return null;
	}

	@Nullable
	public QuickAction getQuickAction(int type, String name, @NonNull Map<String, String> params) {
		for (QuickAction action : quickActions) {
			if (action.getType() == type
					&& (action.hasCustomName(app) && action.getName(app).equals(name) || !action.hasCustomName(app))
					&& action.getParams().equals(params)) {
				return action;
			}
		}
		return null;
	}

	@NonNull
	public FabMarginPreference getFabMarginPref() {
		return fabMarginPref;
	}

	@NonNull
	public CommonPreference<Boolean> getStatePref() {
		return statePref;
	}

	@NonNull
	public CommonPreference<String> getNamePref() {
		return namePref;
	}

	@NonNull
	public CommonPreference<String> getIconPref() {
		return iconPref;
	}

	@NonNull
	public CommonPreference<Integer> getSizePref() {
		return sizePref;
	}

	@NonNull
	public CommonPreference<Float> getOpacityPref() {
		return opacityPref;
	}

	@NonNull
	public CommonPreference<Integer> getCornerRadiusPref() {
		return cornerRadiusPref;
	}

	@NonNull
	public CommonPreference<String> getQuickActionsPref() {
		return quickActionsPref;
	}

	public long getLastModifiedTime() {
		return quickActionsPref.getLastModifiedTime();
	}

	public void setLastModifiedTime(long lastModifiedTime) {
		quickActionsPref.setLastModifiedTime(lastModifiedTime);
	}

	public boolean isSingleAction() {
		return quickActions.size() == 1;
	}

	@Nullable
	@Override
	public Drawable getIcon(boolean nightMode, boolean mapIcon, @ColorInt int color) {
		if (isSingleAction()) {
			QuickAction action = quickActions.get(0);
			Drawable icon = uiUtilities.getPaintedIcon(action.getIconRes(app), color);

			if (mapIcon && action.isActionWithSlash(app)) {
				Drawable slashIcon = uiUtilities.getIcon(nightMode ? R.drawable.ic_action_icon_hide_dark : R.drawable.ic_action_icon_hide_white);
				return new LayerDrawable(new Drawable[] {icon, slashIcon});
			}
			return icon;
		}
		return super.getIcon(nightMode, mapIcon, color);
	}

	public void resetForMode(@NonNull ApplicationMode appMode) {
		statePref.resetModeToDefault(appMode);
		fabMarginPref.resetModeToDefault(appMode);
	}

	public void copyForMode(@NonNull ApplicationMode fromAppMode, @NonNull ApplicationMode toAppMode) {
		statePref.setModeValue(toAppMode, statePref.getModeValue(fromAppMode));
		fabMarginPref.copyForMode(fromAppMode, toAppMode);
	}

	public void saveActions(@NonNull Gson gson) {
		Type type = new TypeToken<List<QuickAction>>() {}.getType();
		quickActionsPref.set(gson.toJson(quickActions, type));
	}

	public void parseQuickActions(@NonNull Gson gson) {
		String json = quickActionsPref.get();

		List<QuickAction> resQuickActions = new ArrayList<>();
		if (!Algorithms.isEmpty(json)) {
			Type type = new TypeToken<List<QuickAction>>() {}.getType();
			List<QuickAction> quickActions = gson.fromJson(json, type);

			if (!Algorithms.isEmpty(quickActions)) {
				for (QuickAction action : quickActions) {
					if (action != null) {
						resQuickActions.add(action);
					}
				}
			}
		}
		this.quickActions = resQuickActions;
	}

	public boolean isDefaultButton() {
		return Algorithms.stringsEqual(DEFAULT_BUTTON_ID, getId());
	}

	@NonNull
	public ButtonAppearanceParams createAppearanceParams() {
		String iconName = null;
		if (iconPref.isSet()) {
			iconName = getIconPref().get();
		}
		if (Algorithms.isEmpty(iconName)) {
			if (isSingleAction()) {
				int iconId = getQuickActions().get(0).getIconRes(app);
				if (iconId > 0) {
					iconName = app.getResources().getResourceEntryName(iconId);
				}
			} else {
				iconName = DEFAULT_ICON_ID;
			}
		}
		return new ButtonAppearanceParams(iconName, getSizePref().get(), getOpacityPref().get(), getCornerRadiusPref().get());
	}

	@NonNull
	@Override
	public String toString() {
		return getId();
	}
}

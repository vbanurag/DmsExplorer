/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.delegate;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.databinding.ContentListActivityBinding;
import net.mm2d.dmsexplorer.util.ItemSelectUtils;
import net.mm2d.dmsexplorer.view.ContentDetailFragment;
import net.mm2d.dmsexplorer.view.base.BaseActivity;
import net.mm2d.dmsexplorer.viewmodel.ContentListActivityModel;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class ContentListActivityDelegateTwoPane extends ContentListActivityDelegate {
    private Fragment mFragment;

    ContentListActivityDelegateTwoPane(@NonNull final BaseActivity activity,
                                       @NonNull final ContentListActivityBinding binding) {
        super(activity, binding);
    }

    @Override
    protected boolean isTwoPane() {
        return true;
    }

    @Override
    public void onSelect(@NonNull final View v, @NonNull final CdsObject object) {
        setDetailFragment(true);
    }

    @Override
    public void onLostSelection() {
        removeDetailFragment();
    }

    @Override
    public void onExecute(@NonNull final View v, @NonNull final CdsObject object, final boolean selected) {
        if (object.hasProtectedResource()) {
            if (!selected) {
                setDetailFragment(true);
            }
            Snackbar.make(v, R.string.toast_not_support_drm, Snackbar.LENGTH_LONG).show();
            return;
        }
        ItemSelectUtils.play(getActivity(), 0);
    }

    private void setDetailFragment(final boolean animate) {
        mFragment = ContentDetailFragment.newInstance();
        if (animate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            @SuppressLint("RtlHardcoded")
            final int gravity = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1
                    ? Gravity.START : Gravity.LEFT;
            mFragment.setEnterTransition(new Slide(gravity));
        }
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.cds_detail_container, mFragment)
                .commitAllowingStateLoss();
    }

    private void removeDetailFragment() {
        if (mFragment == null) {
            return;
        }
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .remove(mFragment)
                .commitAllowingStateLoss();
        mFragment = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        final ContentListActivityModel model = getModel();
        if (model == null) {
            return;
        }
        if (model.isItemSelected()) {
            setDetailFragment(false);
        }
    }
}
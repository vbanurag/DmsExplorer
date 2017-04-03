/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.databinding.ServerDetailFragmentBinding;
import net.mm2d.dmsexplorer.viewmodel.ServerDetailFragmentModel;

/**
 * メディアサーバの詳細情報を表示するFragment。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ServerDetailFragment extends Fragment {
    /**
     * インスタンスを作成する。
     *
     * <p>Bundleの設定と読み出しをこのクラス内で完結させる。
     *
     * @return インスタンス
     */
    public static ServerDetailFragment newInstance() {
        return new ServerDetailFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final Activity activity = getActivity();
        final ServerDetailFragmentBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.server_detail_fragment, container, false);
        final ServerDetailFragmentModel model = ServerDetailFragmentModel.create(activity);
        if (model == null) {
            activity.finish();
            return binding.getRoot();
        }
        binding.setModel(model);
        return binding.getRoot();
    }
}

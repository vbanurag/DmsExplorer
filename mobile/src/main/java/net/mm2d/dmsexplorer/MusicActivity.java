/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import net.mm2d.cds.CdsObject;
import net.mm2d.util.Arib;
import net.mm2d.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class MusicActivity extends AppCompatActivity implements PropertyAdapter.OnItemLinkClickListener {
    private static final String TAG = "MusicActivity";
    private Handler mHandler;
    private MediaPlayer mMediaPlayer;
    private ImageView mArtView;
    private Bitmap mBitmap;
    private ControlView mControlPanel;
    private CdsObject mObject;
    private final OnErrorListener mOnErrorListener = new OnErrorListener() {
        private boolean mNoError = true;

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            if (mNoError) {
                mNoError = false;
                return false;
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_music);
        final Intent intent = getIntent();
        mObject = intent.getParcelableExtra(Const.EXTRA_OBJECT);
        final Uri uri = intent.getData();
        mHandler = new Handler();
        mArtView = (ImageView) findViewById(R.id.art);
        mControlPanel = (ControlView) findViewById(R.id.controlPanel);
        assert mControlPanel != null;
        mControlPanel.setAutoHide(false);
        mControlPanel.setVisible();
        mControlPanel.setOnErrorListener(mOnErrorListener);
        mControlPanel.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                onBackPressed();
            }
        });
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(mControlPanel);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(this, uri);
            mMediaPlayer.prepareAsync();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        final String title = Arib.toDisplayableString(mObject.getTitle());
        actionBar.setTitle(title);
        final int bgColor = Utils.getAccentColor(mObject.getTitle());
        actionBar.setBackgroundDrawable(new ColorDrawable(bgColor));
        mControlPanel.setBackgroundColor(bgColor);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.detail);
        assert recyclerView != null;
        final PropertyAdapter adapter = new PropertyAdapter(this);
        adapter.setOnItemLinkClickListener(this);
        CdsDetailFragment.setupPropertyAdapter(this, adapter, mObject);
        recyclerView.setAdapter(adapter);
        final String albumArtUri = mObject.getValue(CdsObject.UPNP_ALBUM_ART_URI);
        if (albumArtUri != null) {
            new Thread(new GetImage(albumArtUri)).start();
        }
    }

    private class GetImage implements Runnable {
        private final String mUri;

        public GetImage(String uri) {
            mUri = uri;
        }

        @Override
        public void run() {
            try {
                final URL url = new URL(mUri);
                final HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setDoInput(true);
                con.connect();
                if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return;
                }
                final InputStream is = con.getInputStream();
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final byte[] buffer = new byte[1024];
                while (true) {
                    final int size = is.read(buffer);
                    if (size <= 0) {
                        break;
                    }
                    baos.write(buffer, 0, size);
                }
                is.close();
                con.disconnect();
                final byte[] array = baos.toByteArray();
                mBitmap = BitmapFactory.decodeByteArray(array, 0, array.length);
                setImage();
            } catch (final IOException e) {
                Log.w(TAG, e);
            }
        }
    }

    private void setImage() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mArtView.setImageBitmap(mBitmap);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
            mArtView.setImageBitmap(null);
        }
    }

    @Override
    public void onItemLinkClick(String link) {
        final Uri uri = Uri.parse(link);
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(intent);
        } catch (final ActivityNotFoundException e) {
            Log.w(TAG, e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
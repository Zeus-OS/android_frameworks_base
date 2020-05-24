/*
* Copyright (C) 2020 SynthOS
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package com.android.systemui.synth;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.ContentResolver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.android.settingslib.Utils;
import com.android.systemui.R;
import com.android.systemui.statusbar.NotificationMediaManager;

public class MusicText extends RelativeLayout implements NotificationMediaManager.MediaListener{
   private static final boolean DEBUG = false;
   private static final String TAG = "MusicText";

   protected NotificationMediaManager mMediaManager;
   private CharSequence mMediaTitle;
   private CharSequence mMediaArtist;
   private boolean mMediaIsVisible;

   public MusicText(Context context) {
       this(context, null);
   }

   public MusicText(Context context, AttributeSet attrs) {
       this(context, attrs, 0);
   }

   public MusicText(Context context, AttributeSet attrs, int defStyleAttr) {
       this(context, attrs, defStyleAttr, 0);
   }

   public MusicText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
       super(context, attrs, defStyleAttr, defStyleRes);
       if (DEBUG) Log.d(TAG, "new");
   }

   @Override
   public void draw(Canvas canvas) {
       super.draw(canvas);
       if (DEBUG) Log.d(TAG, "draw");
   }

   public void initDependencies (
            NotificationMediaManager mediaManager) {
      mMediaManager = mediaManager;
      mMediaManager.addCallback(this);
   }

   /**
    * Called whenever new media metadata is available.
    * @param metadata New metadata.
    */
   @Override
   public void onMetadataOrStateChanged(MediaMetadata metadata, @PlaybackState.State int state) {
       synchronized (this) {
          boolean nowPlaying = mMediaManager.getNowPlayingTrack() != null;
          boolean nextVisible = NotificationMediaManager.isPlayingState(state) || nowPlaying;
          CharSequence title = null;
          if (metadata != null) {
              title = metadata.getText(MediaMetadata.METADATA_KEY_TITLE);
              if (TextUtils.isEmpty(title)) {
                  title = "Nothing Playing";
              }
          }
          CharSequence artist = metadata == null ? null : metadata.getText(
                  MediaMetadata.METADATA_KEY_ARTIST);

          if (nextVisible == mMediaIsVisible && TextUtils.equals(title, mMediaTitle)
                  && TextUtils.equals(artist, mMediaArtist)) {
              return;
          }

          mMediaTitle = title;
          mMediaArtist = artist;
          mMediaIsVisible = nextVisible;

          if (mMediaTitle == null && nowPlaying) {
              mMediaTitle = mMediaManager.getNowPlayingTrack();
              mMediaIsVisible = true;
              mMediaArtist = null;
          }
          update();
       }
   }

   public void update() {

     ContentResolver resolver = getContext().getContentResolver();
     TextView title = (TextView) findViewById(R.id.title);
     TextView artist = (TextView) findViewById(R.id.artist);

     boolean show = Settings.System.getIntForUser(mContext.getContentResolver(),
             Settings.System.SYNTHOS_MUSIC_VOLUME_PANEL_TEXT, 1, UserHandle.USER_CURRENT) != 0;

      if (mMediaManager != null && mMediaTitle !=  null && mMediaArtist != null) {
        title.setText(mMediaTitle.toString());
        title.setVisibility(show ? View.VISIBLE : View.GONE);
        artist.setText(mMediaArtist.toString());
        artist.setVisibility(show ? View.VISIBLE : View.GONE);
      } else {
        title.setVisibility(View.GONE);
        artist.setVisibility(View.GONE);
      }
   }

}

package i4napps.theeggs;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.qwapi.adclient.android.data.Ad;
import com.qwapi.adclient.android.data.Status;
import com.qwapi.adclient.android.requestparams.AdRequestParams;
import com.qwapi.adclient.android.view.AdEventsListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;

public class MainActivity extends Activity implements AdEventsListener {

	static final int DIALOG_INFO_ID = 0;
	static final int DIALOG_STATS_ID = 1;
	static final int DIALOG_UNLOCK_ID = 2;
	static final int CATCHER_LBOTTOM = 0;
	static final int CATCHER_LTOP = 1;	
	static final int CATCHER_RTOP = 2;
	static final int CATCHER_RBOTTOM = 3;
	static final int GAME_MODE_I = 0;
	static final int GAME_MODE_II = 1;
	static final int STATE_READY = 0;
	static final int STATE_RUNNING = 1;
	static final int STATE_PAUSED = 2;

	private Random mRandom = new Random(System.currentTimeMillis());

	private boolean mUnlocked = false;
	private int mScoreGameI = 0;
	private int mScoreGameII = 0;
	private int mCatcherPosition = CATCHER_LTOP;
	private boolean mHelperVisible = false;
	private boolean mSoundOn = true;
	private boolean mCatchAll = false;
	private int mPenaltiesLimit = 6;



	private Timer mHelperTimer = null;

	private void updateCatcher() {
		// hide/show catcher parts using current catcher position		
		((ImageView)findViewById(R.id.catcherLeft)).setVisibility((mCatcherPosition == CATCHER_LTOP || mCatcherPosition == CATCHER_LBOTTOM ? View.VISIBLE : View.INVISIBLE));
		((ImageView)findViewById(R.id.catcherRight)).setVisibility((mCatcherPosition == CATCHER_RTOP || mCatcherPosition == CATCHER_RBOTTOM ? View.VISIBLE : View.INVISIBLE));		
		((ImageView)findViewById(R.id.catcherLTop)).setVisibility((mCatcherPosition == CATCHER_LTOP ? View.VISIBLE : View.INVISIBLE));
		((ImageView)findViewById(R.id.catcherLBottom)).setVisibility((mCatcherPosition == CATCHER_LBOTTOM ? View.VISIBLE : View.INVISIBLE));
		((ImageView)findViewById(R.id.catcherRTop)).setVisibility((mCatcherPosition == CATCHER_RTOP ? View.VISIBLE : View.INVISIBLE));
		((ImageView)findViewById(R.id.catcherRBottom)).setVisibility((mCatcherPosition == CATCHER_RBOTTOM ? View.VISIBLE : View.INVISIBLE));
	}

	// Need handler for callbacks to the UI thread
	final Handler mHandler = new Handler();

	/* Helper */

	// Create runnable for posting helper updates
	final Runnable mToggleHelper = new Runnable() {
		public void run() {
			mHelperVisible = !mHelperVisible;
			updateHelper();
		}
	};

	private void updateHelper() {
		// hide/show helper
		((ImageView)findViewById(R.id.helper)).setVisibility((mHelperVisible == true ? View.VISIBLE : View.INVISIBLE));
	}

	private class HelperTask extends TimerTask {
		@Override
		public void run() {
			mHandler.post(mToggleHelper);
		}
	}

	// game update
	private int mDigitViewIds[] = null;
	private void initDigitViewIds() {
		mDigitViewIds = new int[10];
		mDigitViewIds[0] = R.drawable.digit0;
		mDigitViewIds[1] = R.drawable.digit1;
		mDigitViewIds[2] = R.drawable.digit2;
		mDigitViewIds[3] = R.drawable.digit3;
		mDigitViewIds[4] = R.drawable.digit4;
		mDigitViewIds[5] = R.drawable.digit5;
		mDigitViewIds[6] = R.drawable.digit6;
		mDigitViewIds[7] = R.drawable.digit7;
		mDigitViewIds[8] = R.drawable.digit8;
		mDigitViewIds[9] = R.drawable.digit9;
	}

	private ImageView mPointsViews[] = null;
	private void initPointsViews() {
		mPointsViews = new ImageView[4];
		mPointsViews[0] = (ImageView) findViewById(R.id.points0);
		mPointsViews[1] = (ImageView) findViewById(R.id.points1);;
		mPointsViews[2] = (ImageView) findViewById(R.id.points2);;
		mPointsViews[3] = (ImageView) findViewById(R.id.points3);;
	}

	private ImageView[][] mProjectileViews = null;

	private void initProjectileViews() {
		mProjectileViews = new ImageView[4][5];
		mProjectileViews[0][0] = (ImageView)findViewById(R.id.proj00);
		mProjectileViews[0][1] = (ImageView)findViewById(R.id.proj01);
		mProjectileViews[0][2] = (ImageView)findViewById(R.id.proj02);
		mProjectileViews[0][3] = (ImageView)findViewById(R.id.proj03);
		mProjectileViews[0][4] = (ImageView)findViewById(R.id.proj04);
		mProjectileViews[1][0] = (ImageView)findViewById(R.id.proj10);
		mProjectileViews[1][1] = (ImageView)findViewById(R.id.proj11);
		mProjectileViews[1][2] = (ImageView)findViewById(R.id.proj12);
		mProjectileViews[1][3] = (ImageView)findViewById(R.id.proj13);
		mProjectileViews[1][4] = (ImageView)findViewById(R.id.proj14);
		mProjectileViews[2][0] = (ImageView)findViewById(R.id.proj20);
		mProjectileViews[2][1] = (ImageView)findViewById(R.id.proj21);
		mProjectileViews[2][2] = (ImageView)findViewById(R.id.proj22);
		mProjectileViews[2][3] = (ImageView)findViewById(R.id.proj23);
		mProjectileViews[2][4] = (ImageView)findViewById(R.id.proj24);
		mProjectileViews[3][0] = (ImageView)findViewById(R.id.proj30);
		mProjectileViews[3][1] = (ImageView)findViewById(R.id.proj31);
		mProjectileViews[3][2] = (ImageView)findViewById(R.id.proj32);
		mProjectileViews[3][3] = (ImageView)findViewById(R.id.proj33);
		mProjectileViews[3][4] = (ImageView)findViewById(R.id.proj34);		

	}

	private void updateTrajectories() {
		// projectiles
		for (int i = 0; i < mProjectileViews.length; i++) {
			for (int j = 0; j < mProjectileViews[i].length; j++) {
				boolean hasProjectile = mGameThread.hasProjectileOn(i, j);
				mProjectileViews[i][j].setVisibility((hasProjectile ? View.VISIBLE : View.INVISIBLE));
			}
		}	
	}

	private void updatePoints() {
		// update points views

		boolean leadintZero = true;
		int points = mGameThread.points;
		int divider = 1000;
		for (int i = 3; i >= 0; i--) {
			int digit = points / divider;
			if (digit == 0 && leadintZero && i > 0) {
				mPointsViews[i].setVisibility(View.INVISIBLE);
			} else {
				leadintZero = false;
				mPointsViews[i].setImageResource(mDigitViewIds[digit]);
				mPointsViews[i].setVisibility(View.VISIBLE);
			}

			points %= divider;
			divider /= 10;
		}
	}

	private ImageView mPenaltyViews[] = null;
	private void initPenalties() {
		mPenaltyViews = new ImageView[3];
		mPenaltyViews[0] = (ImageView) findViewById(R.id.penalty1);
		mPenaltyViews[1] = (ImageView) findViewById(R.id.penalty2);
		mPenaltyViews[2] = (ImageView) findViewById(R.id.penalty3);
	}

	private void updatePenalties() {
		// update penalties views
		if (mGameThread.penalties == 0) {
			for (int i = 0; i < mPenaltyViews.length; i++) {
				mPenaltyViews[i].setVisibility(View.INVISIBLE);
			}
			return;
		}

		for (int i = mGameThread.penalties, idx = 0; i > 0; i -= 2) {
			int oddVisibility = (mPenaltyViews[idx].getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
			mPenaltyViews[idx].setVisibility((i == 1) ? oddVisibility : View.VISIBLE);
			idx++;
		}
	}

	private MediaPlayer mCatchSnd = null;
	private MediaPlayer mCrashSnd = null;
	private MediaPlayer mGameOverSnd = null;
	private MediaPlayer mResetSnd = null;
	private MediaPlayer mMoveSounds[] = null;
	private void initSounds() {
		mCatchSnd = MediaPlayer.create(this, R.raw.snd_catch);
		mCrashSnd = MediaPlayer.create(this, R.raw.snd_crash);
		mGameOverSnd = MediaPlayer.create(this, R.raw.snd_game_over);
		mResetSnd = MediaPlayer.create(this, R.raw.snd_reset);

		mMoveSounds = new MediaPlayer[4];
		mMoveSounds[0] = MediaPlayer.create(this, R.raw.snd_move0);
		mMoveSounds[1] = MediaPlayer.create(this, R.raw.snd_move1);
		mMoveSounds[2] = MediaPlayer.create(this, R.raw.snd_move2);
		mMoveSounds[3] = MediaPlayer.create(this, R.raw.snd_move3);
	}

	private ImageView mLeftCrashViews[] = null;
	private ImageView mRightCrashViews[] = null;
	private void initCrashViews() {
		mLeftCrashViews = new ImageView[5];
		mLeftCrashViews[0] = (ImageView) findViewById(R.id.lcrash0);
		mLeftCrashViews[1] = (ImageView) findViewById(R.id.lcrash1);
		mLeftCrashViews[2] = (ImageView) findViewById(R.id.lcrash2);
		mLeftCrashViews[3] = (ImageView) findViewById(R.id.lcrash3);
		mLeftCrashViews[4] = (ImageView) findViewById(R.id.lcrash4);
		for (int i = 0; i < mLeftCrashViews.length; i++) {
			mLeftCrashViews[i].setVisibility(View.INVISIBLE);
		}

		mRightCrashViews = new ImageView[5];
		mRightCrashViews[0] = (ImageView) findViewById(R.id.rcrash0);
		mRightCrashViews[1] = (ImageView) findViewById(R.id.rcrash1);
		mRightCrashViews[2] = (ImageView) findViewById(R.id.rcrash2);
		mRightCrashViews[3] = (ImageView) findViewById(R.id.rcrash3);
		mRightCrashViews[4] = (ImageView) findViewById(R.id.rcrash4);
		for (int i = 0; i < mRightCrashViews.length; i++) {
			mRightCrashViews[i].setVisibility(View.INVISIBLE);
		}

	}

	private boolean mSaved = false;
	private int mCrashStepIdx = -1;
	final static int SIDE_LEFT = 0;
	final static int SIDE_RIGHT = 1;
	private int mCrashSide = SIDE_LEFT;
	private class CrashThread extends Thread {
		@Override
		public void run() {
			// do the crash "animation"
			int crashSteps = (mSaved ? 5 : 1);
			long delay = (mSaved ? 500 : 1000);	// need at least 500, otherwise sound gets skipped (iphone os works fine though...)
			mCrashStepIdx = 0;
			for (int i = 0; i < crashSteps; i++) {
				mHandler.post(mCrashStep);
				try {
					sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mCrashStepIdx++;
			}

			// clear crashes with no sound
			mCrashStepIdx = -1;
			mHandler.post(mCrashStep);

			// resume after crash
			mHandler.post(mResumeAfterCrash);
		}
	}

	private final Runnable mCrashStep = new Runnable() {
		public void run() {
			// play sound if not hiding
			if (mSoundOn && mCrashStepIdx >= 0) {
				mCrashSnd.start();
			}

			ImageView[] crashViews = (mCrashSide == SIDE_LEFT ? mLeftCrashViews : mRightCrashViews);
			// show one crash view, hide others or hide all
			for (int i = 0; i < crashViews.length; i++) {
				crashViews[i].setVisibility((i == mCrashStepIdx ? View.VISIBLE : View.INVISIBLE));
			}
		}
	};

	private final Runnable mResumeAfterCrash = new Runnable() {
		public void run() {


			// animation completed, update penalties now
			updatePenalties();

			// update projectiles (trajectory views) on the screen (they've been just reset)
			updateTrajectories();

			if (mGameThread.penalties >= 6) {
				mGameThread.state = STATE_READY;

				// game over
				if (mSoundOn) {
					// play game over sound
					mGameOverSnd.start();
				}
				// update score
				if (mGameThread.gameMode == GAME_MODE_I) {
					mScoreGameI = (mScoreGameI < mGameThread.points ? mGameThread.points : mScoreGameI);
				} else {
					mScoreGameII = (mScoreGameII < mGameThread.points ? mGameThread.points : mScoreGameII);
				}
			} else {
				// forget such stuff as pause during crash and just resume
				mGameThread.resumeGame();
			}	
		}
	};

	private class ResetThread extends Thread {
		@Override
		public void run() {
			// do the reset "animation"
			mPenaltiesHidden = true;
			for (int i = 0; i < 6; i++) {
				mHandler.post(mBlinkPenalties);
				try {
					sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mPenaltiesHidden = !mPenaltiesHidden;
			}

			mHandler.post(mResumeAfterReset);
		}	
	};

	private boolean mPenaltiesHidden = false;
	private final Runnable mBlinkPenalties = new Runnable() {
		public void run() {
			if (mSoundOn && !mPenaltiesHidden) {
				mResetSnd.start();
			}

			int visibility = (mPenaltiesHidden ? View.INVISIBLE : View.VISIBLE);
			int lastPenIdx = mGameThread.penalties / 2 + mGameThread.penalties % 2;
			for (int i = 0; i < lastPenIdx; i++) {
				mPenaltyViews[i].setVisibility(visibility);
			}
		}
	};

	private final Runnable mResumeAfterReset = new Runnable() {
		public void run() {
			// set new penalties value
			mGameThread.penalties = 0;

			// clear penalties
			updatePenalties();

			// can go on now
			mGameThread.resumeGame();
			//updatePause();
		}
	};

	final Runnable mGameUpdate = new Runnable() {
		public void run() {
			// update projectiles, points on UI
			Log.d("x", "update game");

			updateTrajectories();
			updatePenalties();

			// check fell down projectiles
			int fellDownId = mGameThread.getFellDownId();

			if (fellDownId >= 0) {	// projectile fell
				// check if fallen projectile has been caught or not
				// by checking current catcher position
				if (mCatchAll) {
					// need to perform on main thread, to see result on screen immediately
					mCatcherPosition = fellDownId;
					updateCatcher();
				}

				if (fellDownId == mCatcherPosition) {
					if (++mGameThread.points > 9999) {
						mGameThread.points = 0;
					}			

					// the projectile is caught
					if (mSoundOn) {
						// play catch sound
						mCatchSnd.start();
					}

					// update points
					updatePoints();

					// check the milestones, if one of them is reached, reset penalties (if any)
					int pointsRem = mGameThread.points % 1000;
					if (mGameThread.penalties > 0
							&& (pointsRem == 0 /* test */ || pointsRem == 200 || pointsRem == 500)) 
					{
						// pause game thread
						mGameThread.suspendGame();
						//updatePause();

						// reset penalties animation (run in a thread)
						new ResetThread().start();
					}
				} else {
					// projectile fell down

					// pause game thread
					mGameThread.suspendGame();
					//updatePause();

					mCrashSide = (fellDownId < 2 ? SIDE_LEFT : SIDE_RIGHT);

					// add penalty points and check the half penalty point condition
					// NOTE: the hidden property of the helperWorker is changed in advance,
					// so the "old" value should be checked
					mSaved = mHelperVisible;
					mGameThread.penalties += (mSaved ? 1 : 2);
					if (mGameThread.penalties > mPenaltiesLimit) mGameThread.penalties = mPenaltiesLimit;


					// update projectile worker delegate and current time frame
					// before starting animation, to allow correct recovery if power goes off 
					// during that animation
					mGameThread.resetProjectiles();

					// submit fall down animation to observer view controller
					// the animation will happen in another thread posting ui updates to main thread
					new CrashThread().start();
				}
			} else {	// no fallen projectile
				if (mSoundOn) {
					// play sound corresponding to moved trajectory
					mMoveSounds[mGameThread.activeTrajId].start();
				}
			}			
		}
	};

	/* Game Thread */
	private GameThread mGameThread = null;

	private class GameThread extends Thread {
		private long mTick;
		private boolean suspended = true;
		public int state = STATE_READY;
		public int gameMode = GAME_MODE_I;
		public int points = 0;
		public int penalties = 0;

		private int maxTraj = 0;
		private int activeTrajId = 0;
		private int trajectoriesNumber = 4;
		private int trajectoryLength = 6;
		private int lengthMask = 0xFF << trajectoryLength;
		private int addNewMask = 0x3;
		private int newProjectileMask = 0x1;
		private int lastActivePositionMask = 0x1 << (trajectoryLength - 2);
		private int emptyTrajectoryMask = 0xFF << (trajectoryLength - 1);
		private int gapCnt = 0;
		private byte trajectories[] = new byte[4];
		private int trajectoryQueueIdx = -1;
		private int trajectoryQueueCnt = 0;
		private byte trajectoriesQueue[] = new byte[4];

		@Override
		public void run() {
			while (true) {
				try {
					sleep(sleepInterval());

					synchronized(this) {
						while (suspended)
							wait();
					}
				} catch (InterruptedException e){
				}

				// update projectiles and post to UI thread
				udpateProjectiles(mTick);

				// notify to UI thread
				mHandler.post(mGameUpdate);

				mTick++;

			}
		}

		private long sleepInterval() {
			float nextTick = 1000;
			long pointsDiv = points % 1000;
			long pointsRem = points % 100;
			// just hardcode it for now
			if (maxTraj == 3) {
				nextTick = (float) ((pointsDiv < 200 ? 0.6 : 0.5) - 0.1 * (points / 1000));
				if (pointsDiv < 200 && pointsRem > 5) {
					nextTick -= (pointsRem < 20 ? 0.1 : 0.15);
				} else if (pointsDiv >= 200 && pointsRem > 5) {
					nextTick -= (pointsRem < 20 ? 0.05 : 0.1);
				}
			} else {
				nextTick = (float) (0.505 - 0.1 * (points / 1000));
				if (pointsRem > 10) {
					nextTick -= (pointsRem < 30 ? 0.05 : 0.1);
				}
				nextTick -= 0.005 * (pointsDiv / 100);
			}

			long result = (long) (1000 * (nextTick <= 0.1 ? 0.1 : nextTick));

			//result = (points < 10 || points > 500 ? 100 : 5);
			//result = (points < 9990 ? 1 : 300);

			return result;
		}

		private int curGap(long tick) {
			if (tick == 0) return 0;
			int gapMin = (points % 100 <= 5 ? 6 : 1);
			int gapMax = (points % 100 <= 20 ? 9 : 6);
			int gap;
			do {
				gap = mRandom.nextInt(10);
			} while(gap < gapMin && gap > gapMax);

			return (gap <= 1 ? 1 : gap);
		}

		private int modeCnt() {
			int cnt = 0;
			for (int i = 0; i < trajectoriesNumber; i++) {
				if ((trajectories[i] & (~emptyTrajectoryMask)) != 0) cnt++;
			}
			return cnt;
		}

		private boolean canAddNew() {
			int tightCount = 0;
			for (int i = 0; i < trajectoriesNumber; i++) {
				if ((trajectories[i] & addNewMask) != 0) tightCount++;
			}

			return (tightCount == maxTraj ? false : true);
		}

		private int nextTrajectoryId() {
			if (trajectoryQueueCnt == 0) return -1;

			do {
				trajectoryQueueIdx = (trajectoryQueueIdx + 1) % trajectoryQueueCnt;
			} while (trajectoriesQueue[trajectoryQueueIdx] < 0);

			return trajectoriesQueue[trajectoryQueueIdx]; // don't forget to increase idx
		}

		private void queueTrajectory(int trajId) {
			// add trajectory to the queue

			// check duplicates
			for (int i = 0; i < trajectoryQueueCnt; i++) {
				if (trajectoriesQueue[i] == trajId) {
					return;	// already queued
				}
			}

			// not yet queued: add
			trajectoriesQueue[trajectoryQueueCnt] = (byte) trajId;
			trajectoryQueueCnt++;	// increase count

			if (trajectoryQueueCnt > trajectoriesNumber) {    				
				Log.e("Error", "Trajectory queueing error");
			}
		}

		private void dequeueTrajectory(int trajId) {
			// remove trajectory from the queue (also shift elements if needed)
			for (int i = 0; i < trajectoryQueueCnt; i++) {
				if (trajectoriesQueue[i] == trajId) {
					// found in queue, remove now
					// shift to the left
					for (int j = i; j < trajectoryQueueCnt - 1; j++) {
						trajectoriesQueue[j] = trajectoriesQueue[j + 1];
					}
					// clear last queue element
					trajectoriesQueue[trajectoryQueueCnt - 1] = 0xF; // (-1)
					trajectoryQueueCnt--;	// decrease count

					// if dequeued element's index is less or equal to current index, then current index must
					// by cycled one position back (to the last position in case of 0th element)
					// this correction is required, because getNextTrajId makes increment (+1) first (do...while loop)
					if (i <= trajectoryQueueIdx) {
						trajectoryQueueIdx = (trajectoryQueueIdx == 0 ? (trajectoriesNumber - 1) : (trajectoryQueueIdx - 1));
					}
					break;
				}
			}
		}

		private void udpateProjectiles(long tick) {
			clearFallenProjectiles();

			// find out which trajectory has to be updated
			activeTrajId = nextTrajectoryId();

			if (activeTrajId >= 0) {
				// move a projectiles on that trajectory
				// if last projectile will move to the last position (fall)
				// then update only that projectile and don't touch others
				if ((trajectories[activeTrajId] & lastActivePositionMask) != 0) {
					// a projectile is about to fall, update only this projectile
					// on given trajectory
					trajectories[activeTrajId] &= ~lastActivePositionMask;	// remove from last active position
					trajectories[activeTrajId] |= (lastActivePositionMask << 1); // add to fallen position
				} else {
					// nobody falls now, so move all projectiles on this trajectory
					trajectories[activeTrajId] <<= 1;	// move projectiles
					trajectories[activeTrajId] &= (~lengthMask);	// clear moved out projectiles
				}
			}

			// if it's time to add a new projectile, then look up
			// for trajectory, which will accept it (must have enough space to 
			// put new projectile, also the mode condition must be kept,
			// which means no more than "mode" trajectories can have projectiles
			// at one moment
			int addTo = -1;
			if (gapCnt >= curGap(tick) && canAddNew() || activeTrajId < 0) {
				gapCnt = -1; // reset gap count (the very next operation is "++")

				do {
					addTo = mRandom.nextInt(trajectoriesNumber);
				} while ((trajectories[addTo] & addNewMask) != 0 // 2 first positions in traj are empty
						|| trajectories[addTo] == 0 && modeCnt() == maxTraj // check mode
						// already "mode" trajs busy, can't add to an empty one
				);

				// set new projectile bit
				trajectories[addTo] |= newProjectileMask;

				// add this trajectory to trajectory queue (duplicates are checked)
				queueTrajectory(addTo);
			}

			if (activeTrajId >= 0) {
				// if active trajectory is empty (don't cout fallen projectiles), then dequeue it
				if ((trajectories[activeTrajId] & (~emptyTrajectoryMask)) == 0) {
					dequeueTrajectory(activeTrajId);
				}
			}

			gapCnt++; // increase new projectile gap count

			// update the active trajectory id, needed for a sound
			if (activeTrajId < 0 && addTo >=0 ) activeTrajId = addTo;

			// that's it, the projectiles are moved and added, queued and dequeued    		
		}

		private void clearFallenProjectiles() {
			for (int i = 0; i < trajectoriesNumber; i++) {
				trajectories[i] &= (~emptyTrajectoryMask);
			}
		}

		public synchronized boolean hasProjectileOn(int trajId, int idx) {
			if ((trajectories[trajId] & (1 << idx)) != 0) return true;
			return false;
		}

		public synchronized boolean projectileFell(int trajId) {
			return hasProjectileOn(trajId, trajectoryLength - 1);
		}

		public synchronized int getFellDownId() {
			for (int i = 0; i < trajectoriesNumber; i++) {
				if (projectileFell(i)) return i;
			}
			return -1;
		}

		public void startGame(int mode) {
			resetGame();
			gameMode = mode;
			maxTraj = (gameMode == GAME_MODE_I ? 3 : 4);
			resumeGame();
			state = STATE_RUNNING;
		}

		public void resetProjectiles() {
			trajectoryQueueCnt = 0;
			trajectoryQueueIdx = -1;
			for (int i = 0; i < trajectories.length; i++) {
				trajectories[i] = 0x0;
			}
			for (int i = 0; i < trajectoriesQueue.length; i++) {
				trajectoriesQueue[i] = 0xF;
			}
		}

		public void resetGame() { 
			mTick = 0;
			points = 0;
			penalties = 0;
			state = STATE_READY;
			activeTrajId = 0;
			gapCnt = 0;
			resetProjectiles();
		}

		public synchronized void suspendGame() {
			if (suspended) return;
			suspended = true;
			notify();
			state = STATE_PAUSED;
		}

		public synchronized void resumeGame() {
			if (!suspended) return;
			suspended = false;
			notify();
			state = STATE_RUNNING;
		}
	}

	private void updateSound() {
		((ImageView)findViewById(R.id.sound)).setVisibility((mSoundOn ? View.VISIBLE : View.INVISIBLE));
	}

	private OnClickListener mInfoListener = new OnClickListener() {
		public void onClick(View v) {
			showDialog(DIALOG_INFO_ID);
		}
	};

	private OnClickListener mStatsListener = new OnClickListener() {
		public void onClick(View v) {
			showDialog(DIALOG_STATS_ID);
		}
	};

	private OnTouchListener mPositionListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {

			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (v == findViewById(R.id.btnLTop)) mCatcherPosition = CATCHER_LTOP;
				if (v == findViewById(R.id.btnLBottom)) mCatcherPosition = CATCHER_LBOTTOM;
				if (v == findViewById(R.id.btnRTop)) mCatcherPosition = CATCHER_RTOP;
				if (v == findViewById(R.id.btnRBottom)) mCatcherPosition = CATCHER_RBOTTOM;

				updateCatcher();
			}

			return false;
		}		
	};

	private OnClickListener mSoundListener = new OnClickListener() {
		public void onClick(View v) {
			mSoundOn = !mSoundOn;
			updateSound();
		}
	};

	private OnClickListener mGameModeListener = new OnClickListener() {
		public void onClick(View v) {
			int mode = (v == findViewById(R.id.btnGameI) ? GAME_MODE_I : GAME_MODE_II);
			if (mGameThread.state != STATE_READY) return;

			// check if game mode 2 is unlocked
			if (mAddDisplayed && mode == GAME_MODE_II && !mUnlocked) {
				showDialog(DIALOG_UNLOCK_ID);
				return;
			}

			mGameThread.startGame(mode);
			updatePoints();
			updatePenalties();
			((ImageView)findViewById(R.id.lblGame1)).setVisibility((mode == GAME_MODE_I ? View.VISIBLE : View.INVISIBLE));
			((ImageView)findViewById(R.id.lblGame2)).setVisibility((mode == GAME_MODE_II ? View.VISIBLE : View.INVISIBLE));
		}
	};

	private void updatePause() {
		int pauseVisible = View.INVISIBLE;
		if (mGameThread.state == STATE_PAUSED) pauseVisible = View.VISIBLE;
		((ImageView)findViewById(R.id.pause)).setVisibility(pauseVisible);		
	}

	private OnTouchListener mPauseListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {

			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (mAddDisplayed && !mUnlocked) {
					showDialog(DIALOG_UNLOCK_ID);
					return false;
				}

				if (mGameThread.state == STATE_RUNNING) {
					mGameThread.suspendGame();
				} else if (mGameThread.state == STATE_PAUSED) {
					mGameThread.resumeGame();
				}

				updatePause();
			}

			return false;
		}	
	};


	public static final String PREFS_NAME = "TheEggsPrefsFile";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// projectile views
		initProjectileViews();

		// digits, points, penalties views
		initDigitViewIds();
		initPointsViews();
		initPenalties();
		initCrashViews();

		// game labels
		((ImageView)findViewById(R.id.lblGame1)).setVisibility(View.INVISIBLE);
		((ImageView)findViewById(R.id.lblGame2)).setVisibility(View.INVISIBLE);


		// set touch listeners for buttons
		ImageButton button = (ImageButton) findViewById(R.id.infoButton);
		button.setOnClickListener(mInfoListener);

		button = (ImageButton) findViewById(R.id.btnStats);
		button.setOnClickListener(mStatsListener);

		// catcher position buttons
		((ImageButton)findViewById(R.id.btnLTop)).setOnTouchListener(mPositionListener);
		((ImageButton)findViewById(R.id.btnLBottom)).setOnTouchListener(mPositionListener);
		((ImageButton)findViewById(R.id.btnRTop)).setOnTouchListener(mPositionListener);
		((ImageButton)findViewById(R.id.btnRBottom)).setOnTouchListener(mPositionListener);

		// sound
		((ImageButton)findViewById(R.id.btnSound)).setOnClickListener(mSoundListener);

		// game mode
		((ImageButton)findViewById(R.id.btnGameI)).setOnClickListener(mGameModeListener);
		((ImageButton)findViewById(R.id.btnGameII)).setOnClickListener(mGameModeListener);	

		// pause
		((ImageButton)findViewById(R.id.btnPause)).setOnTouchListener(mPauseListener);


		// restore scores from preferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		mScoreGameI = settings.getInt("scoreGameI", 0);
		mScoreGameII = settings.getInt("scoreGameII", 0);

		// restore features unlocked status from preferences
		mUnlocked = settings.getBoolean("featuresUnlocked", false);

		// init sounds
		initSounds();

		// create helper timer 
		HelperTask helperTask = new HelperTask();
		mHelperTimer = new Timer(true);
		mHelperTimer.schedule(helperTask, 5000, 5000);

		// game "engine" thread
		mGameThread = new GameThread();
		mGameThread.start();

		// update all views initially
		updateCatcher();
		updateHelper();
		updateSound();
		updatePause();
		updateTrajectories();
		updatePoints();
		updatePenalties();			
	}

	@Override
	protected void onStop () {
		super.onStop();

		mGameThread.suspendGame();
		//updatePause();

		// Save game scores
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("scoreGameI", mScoreGameI);
		editor.putInt("scoreGameII", mScoreGameII);
		//Log.d("onStop", "Save unlocked status: " + mUnlocked);
		editor.putBoolean("featuresUnlocked", mUnlocked);

		// Don't forget to commit your edits!!!
		editor.commit();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mGameThread.suspendGame();
		//updatePause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updatePause();
	}

	@Override
	public Dialog onCreateDialog(int dialogID) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (dialogID) {
		case DIALOG_INFO_ID:
			String message = null;
			try {
				message = getString(R.string.app_name)
				+ "\n" + getString(R.string.by) + " " + getString(R.string.i4napps)
				+ "\n" + "v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}

			builder.setTitle(getString(R.string.about))
			.setMessage(message)
			.setCancelable(false)
			.setPositiveButton(getString(R.string.OK), null);


			break;
		case DIALOG_STATS_ID:
			builder.setTitle(getString(R.string.high_score))
			.setMessage(getString(R.string.game_I) + ":\t" + mScoreGameI 
					+ "\n" + getString(R.string.game_II) + ":\t" + mScoreGameII)
					.setCancelable(false)
					.setPositiveButton(getString(R.string.OK), null);
			break;
		case DIALOG_UNLOCK_ID:
			builder.setTitle(getString(R.string.unlock_title))
			.setMessage(getString(R.string.unlock_msg))
			.setCancelable(false)
			.setPositiveButton(getString(R.string.OK), null);

			break;

		}
		AlertDialog alert = builder.create();

		return alert;
	}

	@Override
	public void onPrepareDialog(int dialogID, Dialog dialog) {
		if (dialogID == DIALOG_STATS_ID) {
			((AlertDialog)dialog).setMessage(getString(R.string.game_I) + ":\t" + mScoreGameI 
					+ "\n" + getString(R.string.game_II) + ":\t" + mScoreGameII);
		}
	}



	public void onAdRequestFailed(Context arg0, AdRequestParams arg1,
			Status arg2) {
		Log.d("ad", "Ad Request Failed");
		// nothing	
	}

	public void onAdRequestSuccessful(Context arg0, AdRequestParams arg1,
			Ad arg2) {
		Log.d("ad", "Ad Request Successful");
		// nothing
	}

	private boolean mAddDisplayed = false;
	public void onDisplayAd(Context arg0, Ad arg1) {
		// nothing
		Log.d("ad", "Ad Displayed");
		((MainActivity)arg0).mAddDisplayed = true;
	}

	public void onAdClick(Context arg0, Ad arg1) {
		// ad click listener
		//Log.d("ad", "Ad Click");
		// luckily for us, Context is exactly the running activity
		((MainActivity)arg0).mUnlocked = true;		
	}

	public void onAdRequest(Context arg0, AdRequestParams arg1) {
		// nothing
	}
}
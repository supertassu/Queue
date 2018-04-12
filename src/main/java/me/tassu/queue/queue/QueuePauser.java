/*
 * This file is part of a project by Tassu_.
 * Usage of this file (or parts of it) is not allowed
 * without a permission from Tassu_.
 *
 * You may contact Tassu_ by e-mailing to <tassu@tassu.me>.
 *
 * Current Package: me.tassu.queue.queue
 *
 * @author tassu
 */

package me.tassu.queue.queue;

import java.util.Date;

public class QueuePauser {

    private boolean paused = false;

    // set to -1 to disable
    private long unpauseDate = -1;

    public void pause() {
        this.paused = true;
    }

    public void unpause() {
        this.paused = false;
    }

    public boolean isPaused() {
        return paused;
    }

    public long getUnpauseDate() {
        return unpauseDate;
    }

    public void setUnpauseDate(long unpauseDate) {
        this.unpauseDate = unpauseDate;
    }
}

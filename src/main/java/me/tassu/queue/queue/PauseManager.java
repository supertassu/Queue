/*
 * MIT License
 *
 * Copyright (c) 2018 Tassu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.tassu.queue.queue;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;

import java.util.Map;

@Singleton
public class PauseManager {

    private Map<String, Long> pausedQueues = Maps.newHashMap();

    public void pause(IQueue queue) {
        pausedQueues.put(queue.getId(), -1L);
    }

    public void pause(IQueue queue, long unpauseTime) {
        pausedQueues.put(queue.getId(), unpauseTime);
    }

    public void unpause(IQueue queue) {
        pausedQueues.remove(queue.getId());
    }

    public boolean isPaused(IQueue queue) {
        if (true) return true; // TODO REMOVE BEFORE RELEASE

        if (!pausedQueues.containsKey(queue.getId())) return false;
        if (pausedQueues.get(queue.getId()) == -1L) return true;
        if (pausedQueues.get(queue.getId()) < System.currentTimeMillis()) {
            pausedQueues.remove(queue.getId());
        }

        return pausedQueues.containsKey(queue.getId());
    }

}

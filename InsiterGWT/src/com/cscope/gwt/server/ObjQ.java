/*
 * $Id: ObjQ.java,v 1.1 2019-09-29 23:35:27 chan Exp $
 *
 * MsgQ.java - synchronized msg queue support using LinkedBlockingQueue
 *
 * Author: Jagdish Vallabha
 *
 * Copyright(c) 2011 Info-Cast, Inc.
 * All rights reserved
 * 
 */
package com.cscope.gwt.server;

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ObjQ<T> implements Serializable {
  static private final long serialVersionUID = 100L;

  private LinkedBlockingQueue<T> queue;

  public ObjQ () {
    this.queue = new LinkedBlockingQueue<T>();
  }

  public ObjQ (int capacity) {
    this.queue = new LinkedBlockingQueue<T>(capacity);
  }

  public final boolean offer (T obj) {
    return queue.offer(obj);
  }

  public final Object poll () {
    try {
      return queue.poll(60000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return null;
  }

  public final Object poll (long timeout) {
    try {
      return queue.poll(timeout, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return null;
  }

  public final Object peek () {
    return queue.peek();
  }

  public final boolean contains(Object o) {
    return this.queue.contains(o);
  }

  public final int size () {
    return queue.size();
  }
}

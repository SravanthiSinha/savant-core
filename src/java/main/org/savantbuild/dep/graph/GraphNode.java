/*
 * Copyright (c) 2001-2006, Inversoft, All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.savantbuild.dep.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * This class is a single node in the artifact graph.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class GraphNode<T, U> {
  private T t;
  private final List<GraphLink<T, U>> outbound = new ArrayList<GraphLink<T, U>>();
  private final List<GraphLink<T, U>> inbound = new ArrayList<GraphLink<T, U>>();

  public GraphNode(T t) {
    this.t = t;
  }

  public T getValue() {
    return t;
  }

  public void setValue(T t) {
    this.t = t;
  }

  void addOutboundLink(GraphNode<T, U> destination, U linkValue) {
    GraphLink<T, U> link = new GraphLink<T, U>(this, destination, linkValue);
    outbound.add(link);
  }

  void addInboundLink(GraphNode<T, U> origin, U linkValue) {
    GraphLink<T, U> link = new GraphLink<T, U>(origin, this, linkValue);
    inbound.add(link);
  }

  /**
   * Removes all inbound links for this node which contain the given value.
   *
   * @param origin    The origin node of this inbound link to remove.
   * @param linkValue The link value to remove. @return  True if the link was removed, false if it doesn't exist.
   */
  public boolean removeInboundLink(GraphNode<T, U> origin, U linkValue) {
    boolean removed = false;
    for (Iterator<GraphLink<T, U>> i = inbound.iterator(); i.hasNext();) {
      GraphLink<T, U> graphLink = i.next();
      if (graphLink.origin.getValue().equals(origin.getValue()) && graphLink.value.equals(linkValue)) {
        i.remove();
        removed = true;
      }
    }

    return removed;
  }

  /**
   * Removes all outbound links for this node which contain the given value.
   *
   * @param destination The destination node of the outbound link to remove.
   * @param linkValue   The link value to remove. @return  True if the link was removed, false if it doesn't exist.
   */
  public boolean removeOutboundLink(GraphNode<T, U> destination, U linkValue) {
    boolean removed = false;
    for (Iterator<GraphLink<T, U>> i = outbound.iterator(); i.hasNext();) {
      GraphLink<T, U> graphLink = i.next();
      if (graphLink.destination.getValue().equals(destination.getValue()) && graphLink.value.equals(linkValue)) {
        i.remove();
        removed = true;
      }
    }

    return removed;
  }

  public List<GraphLink<T, U>> getOutboundLinksList() {
    return new ArrayList<GraphLink<T, U>>(outbound);
  }

  public List<GraphLink<T, U>> getInboundLinksList() {
    return new ArrayList<GraphLink<T, U>>(inbound);
  }

  public GraphLink<T, U> getInboundLink(GraphNode<T, U> origin) {
    for (GraphLink<T, U> link : inbound) {
      if (link.origin.equals(origin)) {
        return link;
      }
    }

    return null;
  }

  public GraphLink<T, U> getOutboundLink(GraphNode<T, U> destination) {
    for (GraphLink<T, U> link : outbound) {
      if (link.destination.equals(destination)) {
        return link;
      }
    }

    return null;
  }

  public String toString() {
    return t.toString();
  }

  void removeLink(GraphLink<T, U> link) {
    outbound.remove(link);
    inbound.remove(link);
  }
}

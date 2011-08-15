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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * This class is used to construct and manage graph structures. This is a simple
 * class that makes the navigation and usage of Graphs simple and accessible.
 * </p>
 * <p/>
 * <h3>Graphs</h3>
 * <p>
 * Graphs are simple structures that model nodes with any number of connections
 * between nodes. The connections are bi-directional and are called Links. A
 * two node graph with a link between the nodes looks like this:
 * </p>
 * <p/>
 * <pre>
 * node1 <---> node2
 * </pre>
 * <p/>
 * <p>
 * The important point about Graphs is that they don't enforce a top level node
 * that controls the entire structure like trees do. Instead, the graph has access
 * to all nodes and the connections between them. This makes finding a Node easy
 * and then traversing the graph also easy.
 * </p>
 * <p/>
 * <h3>Generics</h3>
 * <p>
 * There are two generics for a Graph. The first variable T is the content of
 * the nodes themselves. Each node can stored a single value. The second generic
 * is the value that can be associated with the Link between nodes. This is
 * carried thoroughout the entire graph structure making it very strongly typed.
 * </p>
 * <p/>
 * <h3>Internals</h3>
 * <p>
 * It is important to understand how the Graph works internally. Nodes are stored
 * in a Map whose key is the value for the node. If the graph is storing Strings
 * then only a single node can exist with the value <em>foo</em>. This means that
 * the graph does not allow duplicates. Therefore it would be impossible to have
 * two nodes whose values are <em>foo</em> with different links. The key of the
 * Map is a {@link org.savantbuild.dep.graph.GraphNode} object. The node stores the value as well as all
 * the links.
 * </p>
 * <p/>
 * <h3>Node values</h3>
 * <p>
 * Due to the implementation of the graph, all values must have a good equal
 * and hashcode implementation. Using the object identity is allowed and will
 * then manage the graph based on the heap location of the value objects
 * (pointers are used for the java.lang.Object version of equals and hashcode).
 * </p>
 * <p/>
 * <h3>Thread safety</h3>
 * <p>
 * The Graph is not thread safe. Classes must synchronize on the graph instance
 * in order to protect multi-threaded use.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class HashGraph<T, U> implements Graph<T, U> {
  private Map<T, GraphNode<T, U>> nodes = new HashMap<T, GraphNode<T, U>>();

  public GraphNode<T, U> addGraphNode(T t) {
    GraphNode<T, U> node = nodes.get(t);
    if (node == null) {
      node = new GraphNode<T, U>(t);
      nodes.put(t, node);
    } else {
      node.setValue(t);
    }

    return node;
  }

  public GraphNode<T, U> getGraphNode(T t) {
    return nodes.get(t);
  }

  public boolean contains(T t) {
    return nodes.containsKey(t);
  }

  public void addLink(T origin, T destination, U linkValue) {
    GraphNode<T, U> originNode = addGraphNode(origin);
    GraphNode<T, U> destinationNode = addGraphNode(destination);

    originNode.addOutboundLink(destinationNode, linkValue);
    destinationNode.addInboundLink(originNode, linkValue);
  }

  public void addLink(GraphNode<T, U> origin, GraphNode<T, U> destination, U linkValue) {
    nodes.put(origin.getValue(), origin);
    nodes.put(destination.getValue(), destination);

    origin.addOutboundLink(destination, linkValue);
    destination.addInboundLink(origin, linkValue);
  }

  public void removeLink(T origin, T destination, U linkValue) {
    GraphNode<T, U> originNode = addGraphNode(origin);
    GraphNode<T, U> destinationNode = addGraphNode(destination);
    removeLink(originNode, destinationNode, linkValue);
  }

  public void removeLink(GraphNode<T, U> origin, GraphNode<T, U> destination, U linkValue) {
    // Add the nodes to the graph, even if they aren't there already so that we don't blow up
    nodes.put(origin.getValue(), origin);
    nodes.put(destination.getValue(), destination);

    // Remove the links from the two nodes.
    origin.removeOutboundLink(destination, linkValue);
    destination.removeInboundLink(origin, linkValue);
  }

  public List<GraphLink<T, U>> getOutboundLinks(T t) {
    GraphNode<T, U> node = getGraphNode(t);
    if (node == null) {
      return null;
    }

    return node.getOutboundLinksList();
  }

  public List<GraphLink<T, U>> getInboundLinks(T t) {
    GraphNode<T, U> node = getGraphNode(t);
    if (node == null) {
      return null;
    }

    return node.getInboundLinksList();
  }

  public List<GraphPath<T>> getPaths(T origin, T destination) {
    GraphNode<T, U> originNode = getGraphNode(origin);
    if (originNode == null) {
      return null;
    }

    GraphNode<T, U> destNode = getGraphNode(destination);
    if (destNode == null) {
      return null;
    }

    List<GraphLink<T, U>> originLinks = originNode.getOutboundLinksList();
    List<GraphPath<T>> list = new ArrayList<GraphPath<T>>();
    for (GraphLink<T, U> link : originLinks) {
      if (link.destination.getValue().equals(destination)) {
        GraphPath<T> path = new GraphPath<T>();
        path.addToPath(origin);
        path.addToPath(destination);
        list.add(path);
      } else {
        List<GraphPath<T>> paths = getPaths(link.destination.getValue(), destination);
        for (GraphPath<T> path : paths) {
          path.addToPathHead(origin);
          list.add(path);
        }
      }
    }

    return list;
  }

  /**
   * Returns a Set that contains all of the unique artifacts contained in the graph.
   *
   * @return All the artifacts.
   */
  public Set<T> getAllGraphNodesValues() {
    HashSet<T> set = new HashSet<T>();
    Set<T> keys = nodes.keySet();
    for (T t : keys) {
      set.add(t);
    }
    return set;
  }

  /**
   * @return Returns all the nodes in the graph.
   */
  public Set<GraphNode<T, U>> getAllGraphNodes() {
    return new HashSet<GraphNode<T, U>>(nodes.values());
  }

  public void remove(T value) throws CyclicException {
    GraphNode<T, U> node = nodes.remove(value);
    if (node != null) {
      // Create the sub graph and add the removal node to it
      Set<GraphNode<T, U>> subGraph = new HashSet<GraphNode<T, U>>();
      subGraph.add(node);

      // Grab the sub-graph
      HashSet<GraphNode<T, U>> visited = new HashSet<GraphNode<T, U>>();
      try {
        recurseAdd(node, subGraph, visited);
      } catch (CyclicException e) {
        throw new CyclicException("Cyclic graph [" + e.getMessage() + "]");
      }

      // Recursively remove sub-graphs (depth first) that have no outbounds and all inbounds
      // are marked.
      nodeLoop:
      for (GraphNode<T, U> graphNode : subGraph) {
        List<GraphLink<T, U>> links = graphNode.getInboundLinksList();
        for (GraphLink<T, U> link : links) {

          // If the node has a connection from the outside world, don't clear it out
          if (!subGraph.contains(link.origin) || !subGraph.contains(link.destination)) {
            continue nodeLoop;
          }
        }

        // If all the links are clear, KILL IT! HAHAHAHAHA
        nodes.remove(graphNode.getValue());
        clearLinks(graphNode);
      }

      // Just in case the removal node is reachable, we need to clear out its links
      clearLinks(node);
    }
  }

  private void recurseAdd(GraphNode<T, U> node, Set<GraphNode<T, U>> result, Set<GraphNode<T, U>> visited)
    throws CyclicException {
    if (visited.contains(node)) {
      // Eeck, cyclic
      throw new CyclicException(node.getValue().toString());
    }

    // Depth first
    List<GraphLink<T, U>> links = node.getOutboundLinksList();
    for (GraphLink<T, U> link : links) {
      result.add(link.destination);
      try {
        recurseAdd(link.destination, result, visited);
      } catch (CyclicException e) {
        throw new CyclicException(node.getValue().toString() + "->" + e.getMessage());
      }
    }
  }

  private void clearLinks(GraphNode<T, U> node) {
    List<GraphLink<T, U>> links = node.getOutboundLinksList();
    for (GraphLink<T, U> link : links) {
      node.removeLink(link);
      GraphLink<T, U> inbound = link.destination.getInboundLink(link.origin);
      link.destination.removeLink(inbound);
    }

    links = node.getInboundLinksList();
    for (GraphLink<T, U> link : links) {
      node.removeLink(link);
      GraphLink<T, U> outbound = link.origin.getOutboundLink(link.destination);
      link.origin.removeLink(outbound);
    }
  }
}

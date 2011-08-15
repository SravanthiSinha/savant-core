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

import java.util.List;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * <p>
 * This tests the graph.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class HashGraphTest {
  @Test
  public void addGraphNode() throws Exception {
    HashGraph<String, String> graph = new HashGraph<String, String>();
    graph.addGraphNode("foo");
    assertNotNull(graph.getGraphNode("foo"));
    assertNotNull(graph.contains("foo"));
    assertEquals(1, graph.getAllGraphNodes().size());
    assertEquals(1, graph.getAllGraphNodesValues().size());
  }

  @Test
  public void getGraphNode() throws Exception {
    HashGraph<String, String> graph = new HashGraph<String, String>();
    graph.addGraphNode("foo");
    GraphNode<String, String> node = graph.getGraphNode("foo");
    assertNotNull(node);
    assertEquals("foo", node.getValue());
  }

  @Test
  public void addLink() throws Exception {
    HashGraph<String, String> graph = new HashGraph<String, String>();
    GraphNode<String, String> foo = graph.addGraphNode("foo");
    GraphNode<String, String> bar = graph.addGraphNode("bar");
    graph.addLink("foo", "bar", "link");
    GraphLink<String, String> link = foo.getOutboundLink(bar);
    assertNotNull(link);
    assertEquals("link", link.value);

    link = bar.getInboundLink(foo);
    assertNotNull(link);
    assertEquals("link", link.value);
  }

  @Test
  public void getPaths() throws Exception {
    HashGraph<String, String> graph = new HashGraph<String, String>();
    graph.addGraphNode("one");
    graph.addGraphNode("two");
    graph.addGraphNode("three");
    graph.addGraphNode("four");

    graph.addLink("one", "two", "link");
    graph.addLink("two", "three", "link");
    graph.addLink("three", "four", "link");
    graph.addLink("one", "four", "link");
    graph.addLink("two", "four", "link");

    List<GraphPath<String>> paths = graph.getPaths("one", "four");
    assertEquals(3, paths.size());
    assertEquals(4, paths.get(0).getPath().size());
    assertEquals(3, paths.get(1).getPath().size());
    assertEquals(2, paths.get(2).getPath().size());

    System.out.println(paths.get(0).getPath());
    System.out.println(paths.get(1).getPath());
    System.out.println(paths.get(2).getPath());
  }

  @Test
  public void remove() throws Exception {
    HashGraph<String, String> graph = new HashGraph<String, String>();
    graph.addGraphNode("one");
    graph.addGraphNode("one-two");
    graph.addGraphNode("one-three");
    graph.addGraphNode("two");
    graph.addGraphNode("three");
    graph.addGraphNode("four");
    graph.addGraphNode("five");

    graph.addLink("one", "one-two", "link");
    graph.addLink("one-two", "one-three", "link");
    graph.addLink("one", "two", "link");
    graph.addLink("one", "three", "link");
    graph.addLink("two", "three", "link");
    graph.addLink("two", "four", "link");
    graph.addLink("two", "five", "link");
    graph.addLink("four", "five", "link");
    graph.addLink("four", "one-three", "link");

    graph.remove("two");
    assertEquals(4, graph.getAllGraphNodes().size());
    assertEquals(4, graph.getAllGraphNodesValues().size());
    assertNotNull(graph.getGraphNode("one"));
    assertNotNull(graph.getGraphNode("three"));
    assertNotNull(graph.getGraphNode("one-two"));
    assertNotNull(graph.getGraphNode("one-three"));

    assertEquals(2, graph.getGraphNode("one").getOutboundLinksList().size());
    assertEquals(0, graph.getGraphNode("one").getInboundLinksList().size());
    assertEquals(1, graph.getGraphNode("one-two").getInboundLinksList().size());
    assertEquals(1, graph.getGraphNode("one-two").getOutboundLinksList().size());
    assertEquals(1, graph.getGraphNode("one-three").getInboundLinksList().size());
    assertEquals(0, graph.getGraphNode("one-three").getOutboundLinksList().size());
    assertEquals(1, graph.getGraphNode("three").getInboundLinksList().size());
    assertEquals(0, graph.getGraphNode("three").getOutboundLinksList().size());

    assertEquals("one", graph.getGraphNode("one").getOutboundLink(graph.getGraphNode("one-two")).origin.getValue());
    assertEquals("one-two", graph.getGraphNode("one").getOutboundLink(graph.getGraphNode("one-two")).destination.getValue());
    assertEquals("one", graph.getGraphNode("one-two").getInboundLink(graph.getGraphNode("one")).origin.getValue());
    assertEquals("one-two", graph.getGraphNode("one-two").getInboundLink(graph.getGraphNode("one")).destination.getValue());

    assertEquals("one", graph.getGraphNode("one").getOutboundLink(graph.getGraphNode("three")).origin.getValue());
    assertEquals("three", graph.getGraphNode("one").getOutboundLink(graph.getGraphNode("three")).destination.getValue());
    assertEquals("one", graph.getGraphNode("three").getInboundLink(graph.getGraphNode("one")).origin.getValue());
    assertEquals("three", graph.getGraphNode("three").getInboundLink(graph.getGraphNode("one")).destination.getValue());

    assertEquals("one-two", graph.getGraphNode("one-two").getOutboundLink(graph.getGraphNode("one-three")).origin.getValue());
    assertEquals("one-three", graph.getGraphNode("one-two").getOutboundLink(graph.getGraphNode("one-three")).destination.getValue());
    assertEquals("one-two", graph.getGraphNode("one-three").getInboundLink(graph.getGraphNode("one-two")).origin.getValue());
    assertEquals("one-three", graph.getGraphNode("one-three").getInboundLink(graph.getGraphNode("one-two")).destination.getValue());
  }
}

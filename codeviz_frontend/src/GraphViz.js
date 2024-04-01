/**
 * This example shows how to load a GEXF graph file (using the dedicated
 * graphology parser), and display it with some basic map features: Zoom in and
 * out buttons, reset zoom button, and a slider to increase or decrease the
 * quantity of labels displayed on screen.
 */

import Sigma from "sigma";
import Graph from "graphology";
import { parse } from "graphology-gexf/browser";
// import React from 'react';
import React, { useState, useEffect} from 'react';
import forceAtlas2 from "graphology-layout-forceatlas2";

import RightContext from './RightContext';
import PopUpThreshold from "./PopUpThreshold";
import Legend from "./Legend";

// create shared variable here, so it can edit it
export var hoveredNodeString = null;
export var labelsThresholdRange, thresholdLabel = null;

// Load external GEXF file:
function GraphViz() {
  const [data, setData] = useState(null);
  const initialNodeMessage = "Click on a node to view more options. If the 'Git History' graph is displayed, hover over an edge to view its git history details."
  let hoveredEdge = null;
  const [popUpMenu, setPopUpMenu] = React.useState(false);

  useEffect(() => {
    // Make the API request when the component loads
    fetch('/api/displayGraph')
      .then((response) => response.json())
      .then((responseData) => {
        setData(responseData.file); //extract just the 'file' value
      });
  }, []);

  useEffect(() => {
      const fetchData = async () => {
        const response = await fetch("codeviz_demo.gexf"); //needs to be in 'public' folder // TODO - don't hardcode here
        const gexf = await response.text();
  
        // Parse GEXF string:
        const graph = parse(Graph, gexf);
  
        // Retrieve some useful DOM elements:
        const container = document.getElementsByClassName("graphDisplay--image")[0];
        const zoomInBtn = document.getElementById("zoom-in");
        const zoomOutBtn = document.getElementById("zoom-out");
        const zoomResetBtn = document.getElementById("zoom-reset");

        labelsThresholdRange = document.getElementById("labels-threshold");
        thresholdLabel = document.getElementById("thresholdLabel");
        var labelSize = document.getElementById("label-size");

        const settings = forceAtlas2.inferSettings(graph);
        forceAtlas2.assign(graph, { settings, iterations: 600 });

        // Remove old sigma:
        const rendererOld = new Sigma(graph, container, {
          minCameraRatio: 0.1,
          maxCameraRatio: 10,
        });
        rendererOld.kill();

        // Instantiate new sigma:
        const renderer = new Sigma(graph, container, {
          minCameraRatio: 0.1,
          maxCameraRatio: 10,
          enableEdgeEvents: true,
        });
        const camera = renderer.getCamera();
        renderer.refresh(); // to make sure graph appears right away

        // only reset details when clicking (not dragging) elsewhere
        renderer.on("clickStage", () => {
          document.getElementById("nodeDetails").innerHTML = initialNodeMessage;
          hoveredEdge = null;
          setHoveredNeighbours(graph, renderer);
        });

        renderer.on("enterEdge", (e) => {
          fetch('/api/getEdgeDetails?edgeName=' + e.edge.toString())
              .then((response) => response.json())
              .then((responseData) => {
                if (responseData.string) {
                  hoveredEdge = e.edge;
                  setHoveredNeighbours(graph, renderer);
                  document.getElementById("nodeDetails").innerHTML = responseData.string;
                }
              });
        });
  
        // Bind zoom manipulation buttons
        zoomInBtn.addEventListener("click", () => {
          camera.animatedZoom({ duration: 600 });
        });
        zoomOutBtn.addEventListener("click", () => {
          camera.animatedUnzoom({ duration: 600 });
        });
        zoomResetBtn.addEventListener("click", () => {
          camera.animatedReset({ duration: 600 });
        });

        // Bind labels threshold to range input
        labelsThresholdRange.addEventListener("input", () => {
          renderer.setSetting("labelRenderedSizeThreshold", +labelsThresholdRange.value);
          thresholdLabel.innerHTML = "Threshold: " + labelsThresholdRange.value;
        });

        // Set proper range initial value:
        labelsThresholdRange.value = renderer.getSetting("labelRenderedSizeThreshold") + "";

        labelSize.addEventListener("input", () => {
          renderer.setSetting("labelSize", +labelSize.value);
          thresholdLabel.innerHTML = "Label Size: " + labelSize.value;
        });

        setHoveredNeighbours(graph, renderer);
      };

      function setHoveredNeighbours(graph, renderer){

        // display hovered node's neighbours
        let hoveredNode = undefined;
        let hoveredNeighbors = undefined;

        // Bind graph interactions:
        // also set node in backend, so it can be used by RightContext Menu
        renderer.on("enterNode", ({ node }) => {
          setHoveredNode(node);
          hoveredNodeString = node;
          hoveredEdge = null;
        });
        renderer.on("leaveNode", () => {
          setHoveredNode(undefined);
          hoveredNodeString = null;
        });

        function setHoveredNode(node) {
          if (node) {
            hoveredNode = node;
            hoveredNeighbors = new Set(graph.neighbors(node));
          } else {
            hoveredNode = undefined;
            hoveredNeighbors = undefined;
          }
          // Refresh rendering:
          renderer.refresh();
        }

        renderer.setSetting("nodeReducer", (node, data) => {
          const res = { ...data };
          if (hoveredNeighbors && !hoveredNeighbors.has(node) && hoveredNode !== node) {
            res.label = "";
            res.color = "#C9CDD4"; // should be a little darker than the css colour #E6EAF1
          }
          return res;
        });

        renderer.setSetting("edgeReducer", (edge, data) => {
          const res = { ...data };
          if (hoveredNode && !graph.hasExtremity(edge, hoveredNode)) {
            res.hidden = true; // could set as a colour instead
          } else if (hoveredEdge && hoveredEdge === edge){
            res.color = "#858990";
          }
          return res;
        });
      }
  
      fetchData();
    }, []);


    return (
      <div className="graphDisplay">
        <RightContext>
          <div className="graphDisplay--image"></div>
        </RightContext>
        <div id="controls">
          <div className="center">
            <h2>Controls</h2>
          </div>
          <div className="input"><label htmlFor="zoom-in">Zoom in </label><button id="zoom-in">+</button></div>
          <div className="input"><label htmlFor="zoom-out">Zoom out </label><button id="zoom-out">-</button></div>
          <div className="input"><label htmlFor="zoom-reset">Reset zoom </label><button id="zoom-reset">⊙</button></div>
          <div className="input">
            <label htmlFor="label-size">Label Size </label>
            <input id="label-size" type="range" min="15" max="30" step="0.5" defaultValue={"15"}/>
          <div className="input">
          </div>
            <label htmlFor="labels-threshold">Label Threshold </label>
            <input id="labels-threshold" type="range" min="0" max="15" step="0.5" />
            <p id="thresholdLabel"></p>
            <button onClick={() => setPopUpMenu(true)}>Modify Threshold Settings</button>
            <PopUpThreshold id="popUpThreshold" trigger={popUpMenu} setTrigger={setPopUpMenu}>
            </PopUpThreshold>
          </div>
        </div>
        <Legend />
        <div id="nodeDetailsDisplay">
          <div className="node-help">
            <h2 className="h2">Node/Edge Details:</h2>
            <img src="/info-icon.png" alt='icon' className="info--icon" />
            <p className='tooltip-node'>Information on an node such as class/package that it belongs to and methods within it (if applicable)
            <br/>If the 'Git History' graph is displayed, information on the most recent commit between two nodes.
            </p>
          </div>
            <p id="nodeDetails">
              {initialNodeMessage}
            </p>
        </div>
        </div>
    );
  }

  export default GraphViz
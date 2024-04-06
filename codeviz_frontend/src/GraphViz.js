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

export var selectedColours, setSelectedColours;

// create shared variable here, so it can edit it
export var hoveredNodeString = null;
export var labelsThresholdRange, thresholdLabel = null;

// Load external GEXF file:
function GraphViz() {
  const initialNodeMessage = "Click on a node to view more options. If the 'Git History' graph is displayed, hover over an edge to view its git history details."
  let hoveredEdge = null;
  const [popUpMenu, setPopUpMenu] = React.useState(false);
  let graph = null;
  let renderer = null;

  [selectedColours, setSelectedColours] = useState([]);

      const fetchData = async () => {
        const response = await fetch("codeviz_demo.gexf"); //needs to be in 'public' folder // TODO - don't hardcode here
        const gexf = await response.text();
  
        // Parse GEXF string:
        graph = parse(Graph, gexf);

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
        renderer = new Sigma(graph, container, {
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
        let legendNodes = new Set();

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

          let reduceNode = false;

          // hovered node and neighbours - takes precedence
          if (hoveredNeighbors ){
            if (!hoveredNeighbors.has(node) && hoveredNode !== node) {
              reduceNode = true;
            }
          } else if (selectedColours.length > 0) { // legend colours
            let rgbString = res.color.toString();
            if (selectedColours.includes(rgbString)){
              legendNodes.add(node);
            } else {
              reduceNode = true;
            }
          }

          if (reduceNode){
            res.label = "";
            res.color = "#C9CDD4"; // should be a little darker than the css colour #E6EAF1
          }
          return res;
        });

        renderer.setSetting("edgeReducer", (edge, data) => {
          const res = { ...data };
          if (hoveredNode) {
            if (!graph.hasExtremity(edge, hoveredNode)) { // takes precedence
              res.hidden = true; // could set as a colour instead
            }
          } else if (hoveredEdge && hoveredEdge === edge){
            res.color = "#858990";
          } else if (selectedColours.length > 0) {
            // only show edges if both nodes are selected
            const [source, target] = graph.extremities(edge);
            if (!(legendNodes.has(source) && legendNodes.has(target))) {
              res.hidden = true; // could set as a colour instead
            }
          }
          return res;
        });
      }

    useEffect(() => {
      fetchData();
    }, []);

  useEffect(() => {
    // call fetchData to ensure graph and renderer are set with all the capabilities
    console.log("in useEffect", selectedColours);
    fetchData().then(r => {
      console.log("Selected items changed:", selectedColours);
      // if (graph !== null && renderer !== null) {
      //   setHoveredNeighbours(graph, renderer); // already called in fetchData, don't need to call again
      // }
    });
  }, [selectedColours]); // run when selectedColours changes

  const [controlsExpanded, setControlsExpanded] = useState(false);

  const toggleControls = () => {
    setControlsExpanded(!controlsExpanded);
  };

    return (
      <div className="graphDisplay">
        <RightContext>
          <div className="graphDisplay--image"></div>
        </RightContext>
        <div id="controls" onClick={toggleControls}>
          <div className="center">
            <h3 className="controls-header">Control Panel</h3>
          </div>
          <div className={`controls-content ${controlsExpanded ? 'expanded' : ''}`}>
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
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

// Load external GEXF file:
function GraphViz() {
  const [data, setData] = useState(null);

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

        const labelsThresholdRange = document.getElementById("labels-threshold");
        const updateThresholdSettings = document.getElementById("update-threshold-settings");

        // Instantiate sigma:
        const renderer = new Sigma(graph, container, {
          minCameraRatio: 0.1,
          maxCameraRatio: 10,
        });
        const camera = renderer.getCamera();
        renderer.clear();
  
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
          document.getElementById("thresholdLabel").innerHTML = "Threshold: " + labelsThresholdRange.value;
        });

        // Set proper range initial value:
        labelsThresholdRange.value = renderer.getSetting("labelRenderedSizeThreshold") + "";

        // update threshold settings to match inputs
        updateThresholdSettings.addEventListener("click", () => {
          labelsThresholdRange.min = document.getElementById("min").value;
          labelsThresholdRange.max = document.getElementById("max").value;
          labelsThresholdRange.step = document.getElementById("step").value;
        });

      };
  
      fetchData();
    }, []);


    return (
      <div className="graphDisplay">
        <div className="graphDisplay--image"></div>
        <div id="controls">
          <h2>Controls</h2>
          <div className="input"><label htmlFor="zoom-in">Zoom in </label><button id="zoom-in">+</button></div>
          <div className="input"><label htmlFor="zoom-out">Zoom out </label><button id="zoom-out">-</button></div>
          <div className="input"><label htmlFor="zoom-reset">Reset zoom </label><button id="zoom-reset">⊙</button></div>
          <div className="input">
            <label htmlFor="labels-threshold">Threshold </label>
            <input id="labels-threshold" type="range" min="0" max="15" step="0.5" />
            <table className="center">{/*for ease of use, ideally these values should be set dynamically based on the graph*/}
              <tbody>
              <tr><td>
                <label htmlFor="min">min </label><input id="min" type="number" min="0" step="1" defaultValue={"0"}/>
              </td></tr>
              <tr><td>
                <label htmlFor="max">max </label><input id="max" type="number" max="100" step="1" defaultValue={"15"}/>
              </td></tr>
              <tr><td>
                <label htmlFor="step">step </label><input id="step" type="number" min="0.01" step="0.01" defaultValue={"0.5"}/>
              </td></tr>
              <tr><td>
                <button id="update-threshold-settings">Update Threshold Settings</button>
              </td></tr>
              </tbody>
            </table>
            <p id="thresholdLabel"></p>
          </div>
        </div>
      </div>
    );
  }

  export default GraphViz
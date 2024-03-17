import React, {useEffect} from "react";
import './PopUpThreshold.css';
import './App.css';
import {labelsThresholdRange, thresholdLabel} from './GraphViz'; // read-only

// set values outside the function so their state can be saved
const DEFAULT_MIN = 0;
const DEFAULT_MAX = 15;
const DEFAULT_STEP = 0.5;
let thresholdMin = DEFAULT_MIN;
let thresholdMax = DEFAULT_MAX;
let thresholdStep = DEFAULT_STEP;

function PopUpThreshold(props) {

    const updateThresholdSettings = (props) => {
        // basic error handling
        if (document.getElementById("min").value < 0 || document.getElementById("max").value < 0 || document.getElementById("step").value < 0) {
            document.getElementById("thresholdError").innerHTML = "Negative values not allowed";
        } else if (document.getElementById("min").value > document.getElementById("max").value) {
            document.getElementById("thresholdError").innerHTML = "Min must be less than Max";
        } else if ((document.getElementById("max").value - document.getElementById("min").value) <  document.getElementById("step").value) {
            document.getElementById("thresholdError").innerHTML = "Step must be less than the range";
        } else {
            // values are all good
            thresholdMin = document.getElementById("min").value;
            thresholdMax = document.getElementById("max").value;
            thresholdStep = document.getElementById("step").value;
            labelsThresholdRange.min = document.getElementById("min").value;
            labelsThresholdRange.max = document.getElementById("max").value;
            labelsThresholdRange.step = document.getElementById("step").value;
            thresholdLabel.innerHTML = "Threshold: " + labelsThresholdRange.value; // update label to range change (sometimes applicable)
            props.setTrigger(false);
        }
    };

    const resetDefaults = (props) => {
        document.getElementById("min").value = DEFAULT_MIN;
        document.getElementById("max").value = DEFAULT_MAX;
        document.getElementById("step").value = DEFAULT_STEP;
        updateThresholdSettings(props);
    };

    return (props.trigger) ? (
        <div className="popup">
            <div className="popup-inner">
                <button className="close-btn" onClick={() => props.setTrigger(false)}>cancel</button>
                {props.children}

                <h4>Update Threshold Settings</h4>

                <div className="help-display-threshold">
                    <label htmlFor="min">Min</label>
                    <input id="min" type="number" min="0" max="199" step="1" defaultValue={thresholdMin}/>
                </div>
                <div className="help-display-threshold">
                    <label htmlFor="max">Max</label>
                    <input id="max" type="number" min="1" max="200" step="1" defaultValue={thresholdMax}/>
                </div>
                <div className="help-display-threshold">
                    <label htmlFor="step">Step</label>
                    <input id="step" type="number" min="0.01" max="200" step="0.01" defaultValue={thresholdStep}/>
                </div>

                <font color="red">
                    <b>
                        <p id="thresholdError"></p>
                    </b>
                </font>

                <div className="help-display-threshold">
                    <button id="reset-defaults" onClick={() => resetDefaults(props)}>
                        Reset Defaults
                    </button>
                    <button id="update-threshold-settings" onClick={() => updateThresholdSettings(props)}>
                        Update
                    </button>
                    <img src="/info-icon.png" alt='icon' className="info--icon" />
                    <p className='tooltip'>Adjust the max, min and step values for the threshold slider</p>
                </div>

            </div>
        </div>
    ) : "";
}

export default PopUpThreshold;
import React from "react";
import './PopUpHelp.css';

function PopUpHelp(props) {
    return (props.trigger) ? (
        <div className="popup">
            <div className="popup-inner">
                <button className="close-btn" onClick={() => props.setTrigger(false)}>close</button>
                <h3>Documentation</h3>
                <p>Add documentation...</p>
                {props.children}
            </div>
        </div>
    ) : "";
}

export default PopUpHelp;
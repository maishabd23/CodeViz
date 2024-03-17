import React from "react";
import './RightContext.css';
import {hoveredNodeString} from './GraphViz'; // read-only

//define a functional component for the right-click context menu
function RightContext() {
    //state variables
    const [context, setContext] = React.useState(false);
    const [xyPosition, setxyPosition] = React.useState({ x: 0, y: 0 });
    const [level, setLevel] = React.useState('Class');
    let clickedOption = false

    fetch('/api/getCurrentLevel')
        .then((response) => response.json())
        .then((responseData) => {
            setLevel(responseData.string);
        });

    //event handler for showing the context menu
    const showNavOnNodes = (event) => {
        // if clickedOption is true, that means a menu option was just clicked,
        // so do not show the menu again
        if (hoveredNodeString != null && clickedOption !== true) {
            event.preventDefault();
            setContext(false);
            const positionChange = {
                x: event.pageX,
                y: event.pageY,
            };
            setxyPosition(positionChange);
            setContext(true);
        } else {
            hideContext(); // hide context if a user clicks outside a node
        }
        clickedOption = false;
    };

    //event handler for hiding the context menu
    const hideContext = () => {
        setContext(false);
    };

    //function to set the chosen menu option
    const handleNodeOption = (chosenNodeApi) => {
        clickedOption = true;
        if (hoveredNodeString != null) {
            if (chosenNodeApi === 'generateInnerGraph') {
                fetch('/api/generateInnerGraph?nodeName=' + hoveredNodeString.toString());
            } else {
                fetch('/api/' + chosenNodeApi + '?nodeName=' + hoveredNodeString.toString())
                    .then((response) => response.json())
                    .then((responseData) => {
                        document.getElementById("nodeDetails").innerHTML = responseData.string;
                    });
            }
        } else {
            console.log("ERROR, hoveredNode is " + hoveredNodeString);
        }
    };

    //JavaScript XML (JSX) returned by the component
    return (
        <>
            <div
                className="graphDisplay--image"
                onContextMenu={showNavOnNodes} // right-click on element
                onClick={showNavOnNodes} // (left-)click on element
            >
                {context && (
                    <div
                        style={{ top: xyPosition.y, left: xyPosition.x }}
                        className="rightClick"
                    >
                        <div className="menuElement" onClick={() => handleNodeOption("getNodeDetails")}>
                            View Node Details
                        </div>
                        {level !== 'Package' && (
                            <div className="menuElement" onClick={() => handleNodeOption("getComplexityDetails")}>
                            View Complexity Metrics
                            </div>
                        )}
                        {level !== 'Method' && (
                            <div className="menuElement" onClick={() => handleNodeOption("generateInnerGraph")}>
                                Generate Inner Graph
                            </div>
                        )}
                    </div>
                )}
            </div>
        </>
    );
}
export default RightContext;
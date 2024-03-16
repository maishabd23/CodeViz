import React from "react";
import './RightContext.css';

//define a functional component for the right-click context menu
function RightContext() {
    //state variables
    const [context, setContext] = React.useState(false);
    const [xyPosition, setxyPosition] = React.useState({ x: 0, y: 0 });

    //event handler for showing the context menu
    const showNav = (event) => {
        event.preventDefault();
        setContext(false);
        const positionChange = {
            x: event.pageX,
            y: event.pageY,
        };
        setxyPosition(positionChange);
        setContext(true);
    };

    //event handler for hiding the context menu
    const hideContext = (event) => {
        setContext(false);
    };

    //function to set the chosen menu option
    const [chosenNodeApi, setChosenApi] = React.useState();
    const handleNodeOption = (chosenNodeApi) => {
        setChosenApi(chosenNodeApi);
        // TODO - use renderer
        // renderer.on("enterNode", (e) => {
        //     let selectedNode= e.node;
        //     if (setChosenApi === 'generateInnerGraph'){
        //         fetch('/api/generateInnerGraph?nodeName=' + selectedNode.toString());
        //     } else {
        //         fetch('/api/' + setChosenApi + '?nodeName=' + selectedNode.toString())
        //             .then((response) => response.json())
        //             .then((responseData) => {
        //                 document.getElementById("nodeDetails").innerHTML = responseData.string;
        //             });
        //     }
        // });
    };

    //JavaScript XML (JSX) returned by the component
    return (
        <>
            <div
                className="graphDisplay--image"
                onContextMenu={showNav}
                onClick={hideContext}
            >
                {chosenNodeApi && <h4>"{chosenNodeApi}" is chosen</h4>}
                {context && (
                    <div
                        style={{ top: xyPosition.y, left: xyPosition.x }}
                        className="rightClick"
                    >
                        {/*TODO - disable based on current level*/}
                        <div className="menuElement" onClick={() => handleNodeOption("getNodeDetails")}>
                            View Node Details
                        </div>
                        <div className="menuElement" onClick={() => handleNodeOption("getComplexityDetails")}>
                            View Complexity Metrics
                        </div>
                        <div className="menuElement" onClick={() => handleNodeOption("generateInnerGraph")}>
                            Generate Inner Graph
                        </div>
                    </div>
                )}
            </div>
        </>
    );
}
export default RightContext;
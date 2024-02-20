import React, {useEffect} from 'react';

function Menu() {
    const [milestone, setMilestoneValue] = React.useState('m1');
    const [level, setLevel] = React.useState('Class');

    const handleM1Change = () => {
        fetch('/api/viewGraphLevel?gitHistory=false');
    };

    const handleM2Change = () => {
        fetch('/api/viewGraphLevel?gitHistory=true');
    };

    useEffect(() => {
        const fetchData = async () => {

            const packageView = document.getElementById("package-view");
            const classView = document.getElementById("class-view");
            const methodView = document.getElementById("method-view");
            const clearSearch = document.getElementById("clear-search");

            // Bind view level buttons
            packageView.addEventListener("click", () => {
                fetch('/api/viewGraphLevel?level=PACKAGE');
            });
            classView.addEventListener("click", () => {
                fetch('/api/viewGraphLevel?level=CLASS');
            });
            methodView.addEventListener("click", () => {
                fetch('/api/viewGraphLevel?level=METHOD');
            });
            clearSearch.addEventListener("click", () => {
                fetch('/api/clearSearch');
            });

            fetch('/api/getCurrentLevel')
                .then((response) => response.json())
                .then((responseData) => {
                    document.getElementById("currentLevel").innerHTML = "Current level: " + responseData.string;
                    setLevel(responseData.string);
                });

            fetch('/api/getCurrentMilestone')
                .then((response) => response.json())
                .then((responseData) => {
                    setMilestoneValue(responseData.string);
                });

            const search = document.getElementById("searchInput");
            search.addEventListener("search", mySearchFunction);
            function mySearchFunction() {
                var x = document.getElementById("searchInput");
                document.getElementById("printSearch").innerHTML = "Searching for: " + x.value;
                fetch('/api/searchGraph?searchValue=' + x.value)
                    .then((response) => response.json())
                    .then((responseData) => {
                        document.getElementById("printSearch").innerHTML = responseData.string;
                    });
            }

            const detailedSearch = document.getElementById("detailedSearchInput");
            detailedSearch.addEventListener("search", myDetailedSearchFunction);
            function myDetailedSearchFunction() {
                var x = document.getElementById("detailedSearchInput");
                document.getElementById("printSearch").innerHTML = "Searching for: " + x.value;
                fetch('/api/searchGraph?detailed=true&searchValue=' + x.value)
                    .then((response) => response.json())
                    .then((responseData) => {
                        document.getElementById("printSearch").innerHTML = responseData.string;
                    });
            }

        };

        fetchData();
    }, []);

    return (
        <div className='menu'>
            <h2>Menu</h2>
            {/*TODO - add submit button?*/}
            {/*TODO - remove duplicate search bars and use dropdown instead - either simple or detailed search*/}
            <div id="menu-controls">
                <h3>Search</h3>
                <table className="center">
                    <tbody>
                    <tr><td>
                        <div className="help-display">
                            <input type="search" id="searchInput" placeholder="Simple Search..."/>
                            <img src="/info-icon.png" alt='icon' className="info--icon" />
                            <p className='tooltip'>Search for specific node names</p>
                        </div>
                    </td></tr>
                    <tr><td>
                        <div className="help-display">
                            <input type="search" id="detailedSearchInput" placeholder="Detailed Search..."/>
                            <img src="/info-icon.png" alt='icon' className="info--icon" />
                            <p className='tooltip'>Search for node names, connections, arguments/return types, etc</p>
                        </div>
                    </td></tr>
                    <tr><td>
                        <div className="center">
                            <label htmlFor="clear-search"></label><button id="clear-search">Clear Search</button>
                        </div>
                    </td></tr>
                    </tbody>
                </table>
                        <p id="printSearch"></p>
            </div>

            <div id="menu-controls">
                <h3>Switch Level</h3>
                <p id="currentLevel"></p>
                <table className="center">
                    <tbody>
                    <tr>
                        <td>
                            <div className="input"><label htmlFor="package-view"></label><button id="package-view">Package</button></div>
                        </td>
                        <td>
                            <div className="input"><label htmlFor="class-view"></label><button id="class-view">Class</button></div>
                        </td>
                        <td>
                            <div className="input"><label htmlFor="method-view"></label><button id="method-view">Method</button></div>
                        </td>
                        <td>
                            <div>
                                <img src="/info-icon.png" alt='icon' className="info--icon" />
                                <p className='tooltip'>Level of granularity at which to display the graph</p>
                            </div>
                        </td>
                    </tr>
                    </tbody>
                </table>

                <div className="help-display">
                <input type="radio" id="M1" name="graph_type" value={milestone === 'm1'} onChange={handleM1Change} checked={milestone === 'm1'}>
                </input>
                <label htmlFor="M1">Dependency</label>
                <input type="radio" id="M2" name="graph_type" value={milestone === 'm2'} onChange={handleM2Change} checked={milestone === 'm2'} disabled={level !== 'Class'} >
                </input>
                <label htmlFor="M2">Git History</label>
                    <img src="/info-icon.png" alt='icon' className="info--icon" />
                    <p className='tooltip'>Graph type to view. Note: Git History graph is only available for class view</p>
                </div>

            </div>
        </div>
    );
}

export default Menu;
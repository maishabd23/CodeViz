import React, {useEffect} from 'react';

function Menu() {

    useEffect(() => {
        const fetchData = async () => {

            const packageView = document.getElementById("package-view");
            const classView = document.getElementById("class-view");
            const methodView = document.getElementById("method-view");
            const clearSearch = document.getElementById("clear-search");
            const clearFilter = document.getElementById("clear-filter");

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
            clearFilter.addEventListener("click", () => {
                fetch('/api/clearSelectedNode');
            });

            fetch('/api/getCurrentLevel')
                .then((response) => response.json())
                .then((responseData) => {
                    document.getElementById("currentLevel").innerHTML = "Current level: " + responseData.string;
                });

            fetch('/api/getFilteredNode')
                .then((response) => response.json())
                .then((responseData) => {
                    document.getElementById("currentFilter").innerHTML = "Current filter: " + responseData.string;
                });

            const search = document.getElementById("searchInput");
            search.addEventListener("search", mySearchFunction);
            function mySearchFunction() {
                var x = document.getElementById("searchInput");
                document.getElementById("printSearch").innerHTML = "Searching for: " + x.value;
                fetch('/api/viewGraphLevel?searchValue=' + x.value);
            }

            const detailedSearch = document.getElementById("detailedSearchInput");
            detailedSearch.addEventListener("search", myDetailedSearchFunction);
            function myDetailedSearchFunction() {
                var x = document.getElementById("detailedSearchInput");
                document.getElementById("printSearch").innerHTML = "Searching for: " + x.value;
                fetch('/api/viewGraphLevel?detailed=true&searchValue=' + x.value);
            }

            fetch('/api/getSearchResult')
                .then((response) => response.json())
                .then((responseData) => {
                    document.getElementById("printSearch").innerHTML = responseData.string;
                });


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
                <p id="currentFilter"></p>
                <div className="input"><label htmlFor="clear-filter"></label><button id="clear-filter">Clear Filter</button></div>

            </div>
        </div>
    );
}

export default Menu;
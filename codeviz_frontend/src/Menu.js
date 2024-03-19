import React, { useEffect, useState } from 'react';

function Menu() {
    const [milestone, setMilestoneValue] = useState('m1');
    const [level, setLevel] = useState('Class');
    const [searchValue, setSearchValue] = useState('');
    const [searchClasses, setSearchClasses] = useState(false);
    const [searchMethods, setSearchMethods] = useState(false);
    const [searchAttributes, setSearchAttributes] = useState(false);
    const [searchParameters, setSearchParameters] = useState(false);
    const [searchReturnType, setSearchReturnType] = useState(false);
    const [searchConnections, setSearchConnections] = useState(false);

    //FOR VIEWING REPO
    const [repoURL, setRepoURL] = useState('');
    const [submitted, setSubmitted] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);


    // Function to handle changes in the repoURL input field
    const handleRepoURLChange = (e) => {
        const newRepoURL = e.target.value;
        setRepoURL(newRepoURL);
    };

    // Function to handle form submission
    const handleSubmit = () => {
        setLoading(true);
        // Make a POST request to initialize CodeVizController with repoURL
        const isSuccessful = fetch('/init', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({repoURL}),
        })
            .then(response => {
                console.log(response);
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                // Handle successful response
                console.log("SUCCESS!");
                console.log('Response data:', data);
                setSubmitted(true);
            })
            .catch(error => {
                // Handle error
                console.error('Error:', error);
                setError(error.message || 'An unexpected error occurred');
            })
            .finally(() => {
                setLoading(false);
            });
        console.log(isSuccessful)
    };


    const handleM1Change = () => {
        fetch('/api/annotateGraph?gitHistory=false');
    };

    const handleM2Change = () => {
        fetch('/api/annotateGraph?gitHistory=true');
    };
    const mySearchFunction = async () => {
        // Constructing the search query object
        const searchQuery = {
            value: searchValue,
            searchClasses: level === 'Class' ? searchClasses : false,
            searchMethods: level === 'Class' ? searchMethods : level === 'Method',
            searchAttributes: level === 'Class' ? searchAttributes : false,
            searchParameters: level === 'Method' ? searchParameters : false,
            searchReturnType: level === 'Method' ? searchReturnType : false,
            searchConnections: level === 'Package' ? searchConnections : false,
        };
        // Use fetch to send the search query to backend API
        try {
            const response = await fetch('/api/searchGraph', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(searchQuery),
            });
            const data = await response.json();
            document.getElementById("printSearch").innerHTML = data.string;
        } catch (error) {
            console.error('Error:', error);
        }
    };

    const clearSearch = async () => {
        // This sets the front end state back to its initial state, if necessary
        setSearchValue('');
        setSearchClasses(false);
        setSearchMethods(false);
        setSearchAttributes(false);
        setSearchParameters(false);
        setSearchReturnType(false);
        setSearchConnections(false);

        // Now, call the backend API to clear the search there as well
        try {
            const response = await fetch('/api/clearSearch', { method: 'GET' });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            // Optionally, process the response if it returns any data
            const responseData = await response.json();
            console.log(responseData);
            // Update any state or UI as necessary
        } catch (error) {
            console.error("Failed to clear search: ", error);
        }

        // Optionally, clear any displayed search results in the UI
        document.getElementById("printSearch").innerHTML = "";
    };

    <button id="clear-search" onClick={clearSearch}>Clear Search</button>

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

            fetch('/api/getCurrentGraphName')
                .then((response) => response.json())
                .then((responseData) => {
                    document.getElementById("currentLevel").innerHTML = "Current level: " + responseData.string;
                });

            fetch('/api/getCurrentLevel')
                .then((response) => response.json())
                .then((responseData) => {
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
                const searchValue = document.getElementById("searchInput").value;
                // Construct the search query object
                const searchQuery = {
                    value: searchValue,
                    searchClasses: level === 'Class' ? searchClasses : false,
                    searchMethods: level === 'Class' ? searchMethods : level === 'Method',
                    searchAttributes: level === 'Class' ? searchAttributes : false,
                    searchParameters: level === 'Method' ? searchParameters : false,
                    searchReturnType: level === 'Method' ? searchReturnType : false,
                    searchConnections: level === 'Package' ? searchConnections : false,
                };

                // Use fetch to send the search query to backend API
                fetch('/api/searchGraph', {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(searchQuery),
                })
                    .then(response => response.json())
                    .then(data => {
                        document.getElementById("printSearch").innerHTML = data.string;
                    })
                    .catch((error) => {
                        console.error('Error:', error);
                    });
            }


            /*const detailedSearch = document.getElementById("detailedSearchInput");
            detailedSearch.addEventListener("search", myDetailedSearchFunction);
            function myDetailedSearchFunction() {
                var x = document.getElementById("detailedSearchInput");
                document.getElementById("printSearch").innerHTML = "Searching for: " + x.value;
                fetch('/api/searchGraph?detailed=true&searchValue=' + x.value)
                    .then((response) => response.json())
                    .then((responseData) => {
                        document.getElementById("printSearch").innerHTML = responseData.string;
                    });
            }*/

        };
        fetchData();

    }, [level]);

    return (
        <div className='menu'>
            <h2>Menu</h2>
            <div id="menu-controls">
                <h3>View Repo</h3>
                <table className="center">
                    <tbody>
                    <tr><td>
                        <div className="help-display">
                            <input type="text" value={repoURL} onChange={handleRepoURLChange} placeholder="Enter Repository URL" />
                            <button onClick={handleSubmit} disabled={loading}>Submit</button>
                            {error && <p className="error">{error}</p>}
                            {loading && <p>Loading...</p>}
                        </div>
                    </td></tr>
                    </tbody>
                </table>
            </div>

            <div id="menu-controls">
                <h3>Search</h3>
                <table className="center">
                    <tbody>
                    <tr>
                        <td>
                            <div className="help-display">
                                <input
                                    type="search"
                                    id="searchInput"
                                    placeholder="Input a search term"
                                />
                                <img src="/info-icon.png" alt='icon' className="info--icon" />
                                <p className='tooltip'>Search for specific node names</p>
                            </div>
                        </td>
                    </tr>
                    </tbody>
                </table>
                <button onClick={mySearchFunction}>Search</button>
                {/* ... */}
            </div>

        {/* Conditional rendering based on 'level' */}
            {level === 'Package' && (
                <>
                    <label>
                        <input type="checkbox" checked={searchClasses} onChange={(e) => setSearchClasses(e.target.checked)} />
                        Classes
                    </label>
                    <label>
                        <input type="checkbox" checked={searchConnections} onChange={(e) => setSearchConnections(e.target.checked)} />
                        Connections
                    </label>
                </>
            )}
            {level === 'Class' && (
                <>
                    <label>
                        <input type="checkbox" checked={searchMethods} onChange={(e) => setSearchMethods(e.target.checked)} />
                        Methods
                    </label>
                    <label>
                        <input type="checkbox" checked={searchAttributes} onChange={(e) => setSearchAttributes(e.target.checked)} />
                        Attributes
                    </label>
                </>
            )}
            {level === 'Method' && (
                <>
                    <label>
                        <input type="checkbox" checked={searchParameters} onChange={(e) => setSearchParameters(e.target.checked)} />
                        Parameters
                    </label>
                    <label>
                        <input type="checkbox" checked={searchReturnType} onChange={(e) => setSearchReturnType(e.target.checked)} />
                        Return Type
                    </label>
                </>
            )}
            <button id="clear-search" onClick={() => { /* Implement clear search logic */ }}>Clear Search</button>
            <p id="printSearch"></p>

        <div id="switch-level-controls">
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
        </div>

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
    );
}

export default Menu;
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
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [searchResult, setSearchResult] = useState(null);

    // Function to handle changes in the repoURL input field
    const handleRepoURLChange = (e) => {
        const newRepoURL = e.target.value;
        setRepoURL(newRepoURL);
    };

    // Function to handle form submission
    const handleSubmit = () => {
        setLoading(true);
        setError(false); // if retrying, set error to false
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
                if (data.ok === 'true') {
                    // Handle successful response
                    console.log('SUCCESS!');
                    console.log('Response data:', data);
                } else {
                    // Handle error response
                    console.error('Error:', data.error || 'An unexpected error occurred');
                    setError(data.error || 'An unexpected error occurred');
                }
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

    const handleKeyPress = (event, buttonId) => {
        if (event.key === 'Enter') {
            event.preventDefault(); // Prevent the default form submission
            document.getElementById(buttonId).click(); // Trigger click event on the button
        }
    };

    const handleM1Change = () => {
        fetch('/api/annotateGraph?gitHistory=false');
    };

    const handleM2Change = () => {
        fetch('/api/annotateGraph?gitHistory=true');
    };
    const mySearchFunction = async () => {
        const searchValue = document.getElementById("searchInput").value;
        // Constructing the search query object
        const searchQuery = {
            value: searchValue,

            // level itself can be checked by adjacent levels
            searchClasses: level !== 'Method' ? searchClasses : false, // only Method cannot search classes
            searchMethods: level !== 'Package' ? searchMethods : false, // only Package cannot search methods

            // only the specific level can check its own inner details
            searchAttributes: level === 'Class' ? searchAttributes : false,
            searchParameters: level === 'Method' ? searchParameters : false,
            searchReturnType: level === 'Method' ? searchReturnType : false,
            searchConnections: searchConnections,
        };
        // Use fetch to send the search query to backend API
        try {
            const response = await fetch('/api/searchGraph', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(searchQuery),
            });
            const data = await response.json();
            setSearchResult(data.string)
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

        };
        fetchData();

    }, [level]);

    return (
        <div className='menu'>
            <div id="menu-controls">
                <h3>Select Repository</h3>
                <div>
                    <input type="search" id="viewRepoInput" value={repoURL}
                           onChange={handleRepoURLChange}
                           onKeyDown={(event) => handleKeyPress(event, 'viewRepoButton')}
                           placeholder="Enter Repository URL" />
                    <button id="viewRepoButton" type="submit" onClick={handleSubmit} disabled={loading}>Submit</button>
                    {error && <p className="error">{error}</p>}
                    {loading && <p>Loading...</p>}
                </div>
            </div>

            <div id="menu-controls">
                <h3>Search</h3>
                <div className="help-display">
                    <input
                        type="search"
                        id="searchInput"
                        placeholder="Enter a search term"
                        onKeyDown={(event) => handleKeyPress(event, 'searchButton')}
                    />
                    <img src="/info-icon.png" alt='icon' className="info--icon" />
                    <p className='tooltip'>Search for specific node names</p>
                </div>

                {/* Conditional rendering based on 'level' */}
                {level === 'Package' && (
                    <>
                    <div className="checkbox-container">
                    <div>
                        <label>
                            <input type="checkbox" checked={searchClasses} onChange={(e) => setSearchClasses(e.target.checked)} />
                            Classes
                        </label>
                        </div>
                        <div>
                        <label>
                            <input type="checkbox" checked={searchConnections} onChange={(e) => setSearchConnections(e.target.checked)} />
                            Connections
                        </label>
                        </div>
                    </div>
                    </>
                )}
                {level === 'Class' && (
                    <>
                    <div className="checkbox-container">
                    <div>
                        <label>
                            <input type="checkbox" checked={searchMethods} onChange={(e) => setSearchMethods(e.target.checked)} />
                            Methods
                        </label>
                    </div>
                    <div>
                        <label>
                            <input type="checkbox" checked={searchAttributes} onChange={(e) => setSearchAttributes(e.target.checked)} />
                            Attributes
                        </label>
                    </div>
                    <div>
                        <label>
                            <input type="checkbox" checked={searchConnections} onChange={(e) => setSearchConnections(e.target.checked)} />
                            Connections
                        </label>
                    </div>
                    </div>
                    </>
                )}
                {level === 'Method' && (
                    <>
                    <div className="checkbox-container">
                    <div>
                        <label>
                            <input type="checkbox" checked={searchParameters} onChange={(e) => setSearchParameters(e.target.checked)} />
                            Parameters
                        </label>
                    </div>
                    <div>
                        <label>
                            <input type="checkbox" checked={searchReturnType} onChange={(e) => setSearchReturnType(e.target.checked)} />
                            Return Type
                        </label>
                    </div>
                    <div>
                        <label>
                            <input type="checkbox" checked={searchConnections} onChange={(e) => setSearchConnections(e.target.checked)} />
                            Connections
                        </label>
                    </div>
                    </div>
                    </>
                )}

                <button id="searchButton" onClick={mySearchFunction}>Search</button>
                <button id="clear-search" onClick={() => { /* Implement clear search logic */ }}>Clear Search</button>
                {searchResult && (
                    <div id="printSearch" dangerouslySetInnerHTML={{ __html: searchResult }}></div>
                )}

            </div>

            <div id="menu-controls">
                <h3>Switch Level</h3>
                <p id="currentLevel"></p>
                <div className="help-display">
                    <div className="input"><label htmlFor="package-view"></label><button id="package-view">Package</button></div>
                    <div className="input"><label htmlFor="class-view"></label><button id="class-view">Class</button></div>
                    <div className="input"><label htmlFor="method-view"></label><button id="method-view">Method</button></div>
                    <div>
                        <img src="/info-icon.png" alt='icon' className="info--icon" />
                        <p className='tooltip'>Level of granularity at which to display the graph</p>
                    </div>
                </div>

                <div className="graph-type">
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
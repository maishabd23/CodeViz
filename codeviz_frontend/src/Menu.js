import React, {useEffect, useState} from 'react';

function Menu() {
    const [milestone, setMilestoneValue] = React.useState('m1');
    const [level, setLevel] = React.useState('Class');

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
                    setSubmitted(true);
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


    const handleM1Change = () => {
        fetch('/api/annotateGraph?gitHistory=false');
    };

    const handleM2Change = () => {
        fetch('/api/annotateGraph?gitHistory=true');
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

            // TODO fix this
            // const repoUrlInputElement = document.getElementById("viewRepoInput");
            // repoUrlInputElement.addEventListener("search", handleSubmit);

        };

        fetchData();
    }, []);

    return (
        <div className='menu'>
            <div id="menu-controls">
                <h3>Select Repository</h3>
                <table className="center">
                    <tbody>
                    <tr>
                        <td>
                        <div className="chooseRepo">
                            <input type="search" id="viewRepoInput" value={repoURL} onChange={handleRepoURLChange} placeholder="Enter Repository URL" />
                            <button type="submit" onClick={handleSubmit} disabled={loading}>Submit</button>
                            {error && <p className="error">{error}</p>}
                            {loading && <p>Loading...</p>}
                        </div>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>

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
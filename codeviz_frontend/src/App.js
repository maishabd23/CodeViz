import React, {useEffect, useState} from 'react';
import Navbar from './Navbar';
import MainSection from './MainSection';
import './App.css';

function App() {
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

    useEffect(() => {
        fetch('/api/isDisplayingGraph')
            .then(response => response.json())
            .then(responseData => {
                setSubmitted(responseData.string);
            })
            .catch(error => {
                console.error('Error:', error);
                setError(error.message || 'An unexpected error occurred');
            });
    }, []); // Empty dependency array ensures that this effect runs only once after the initial render

    // Render the form if not submitted, otherwise render Navbar and MainSection
    return (
        <div className="App">
            {submitted ? (
                <>
                    <Navbar />
                    <MainSection />
                </>
            ) : (
                <>
                    <input type="text" value={repoURL} onChange={handleRepoURLChange} placeholder="Enter Repository URL" />
                    <button onClick={handleSubmit} disabled={loading}>Submit</button>
                    {error && <p className="error">{error}</p>}
                    {loading && <p>Loading...</p>}
                </>
            )}
        </div>
    );
}

export default App;

import React, {useEffect, useState} from 'react';
import Navbar from './Navbar';
import MainSection from './MainSection';
import './App.css';

function App() {
    // Render the form if not submitted, otherwise render Navbar and MainSection
    return (
        <div className="App">
            <>
                <Navbar />
                <MainSection />
            </>
        </div>
    );
}

export default App;

import React, { useEffect, useState } from 'react';

function Graph() {
  const [data, setData] = useState(null);

  useEffect(() => {
    // Make the API request when the component loads
    fetch('/api/displayGraph')
      .then((response) => response.json())
      .then((responseData) => {
        setData(responseData.file); //extract just the 'file' value
      });
  }, []);

  return (
    <div className='graphDisplay'>
      {data ? ( 
        <img className='graphDisplay--image' src={data} alt="Sample graph"></img> //if data is available, print
      ) : (
        <p>Loading...</p> //else, print loading...
      )}
    </div>
  );
}

export default Graph;

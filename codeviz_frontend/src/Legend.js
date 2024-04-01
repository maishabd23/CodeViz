import React, { useEffect } from 'react';

import './Legend.css';
import legendHtml from './LegendContent';

function Legend() {
    const handleLegendItemClick = (category) => {
        // Logic to handle behavior when a legend option is pressed
        console.log(`Legend option "${category}" pressed`);
    };

    useEffect(() => {
        const legendItems = document.querySelectorAll('.legend-item');
        legendItems.forEach(item => {
            item.addEventListener('click', () => {
                const category = item.dataset.category;
                handleLegendItemClick(category);
            });
        });

        // Clean up event listeners when component unmounts
        return () => {
            legendItems.forEach(item => {
                item.removeEventListener('click', handleLegendItemClick);
            });
        };
    }, []); // Run only once on component mount

    return <div dangerouslySetInnerHTML={{ __html: legendHtml }} />;

}

export default Legend;

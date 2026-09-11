document.addEventListener("DOMContentLoaded", function () {

    if (!cityData || cityData.length === 0) {
        return;
    }

    let labels = [];
    let values = [];

    cityData.forEach(function (item) {
        labels.push(item[0]);
        values.push(item[1]);
    });

    const ctx = document.getElementById("cityChart");

    new Chart(ctx, {

        type: "doughnut",

        data: {
            labels: labels,

            datasets: [{
                data: values,

                backgroundColor: [
                    "#3B82F6",
                    "#10B981",
                    "#F59E0B",
                    "#EF4444",
                    "#8B5CF6",
                    "#06B6D4",
                    "#F97316",
                    "#14B8A6"
                ],

                borderColor: "#ffffff",
                borderWidth: 3,

                cutout: "60%",
                radius: "75%"
            }]
        },

        options: {

            responsive: true,
            maintainAspectRatio: false,
			
			cutout: "60%",
			radius: "75%",

            plugins: {

                legend: {

                    position: "right",

                    labels: {

                        boxWidth: 14,
                        padding: 15,
                        font: {
                            size: 13
                        }

                    }

                }

            }

        }

    });

});
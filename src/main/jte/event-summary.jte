@param de.bas.bodo.woodle.view.ScheduleEventStep1Form step1FormData
@param de.bas.bodo.woodle.view.ScheduleEventStep2Form step2FormData
@param de.bas.bodo.woodle.view.ScheduleEventStep3Form step3FormData
@param String pollUrl

<!DOCTYPE html>
<html>
<head>
    <title>Poll Summary</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 20px;
            text-align: center;
        }
        h1 {
            color: #333;
        }
        .logo {
            max-width: 200px;
            margin-bottom: 20px;
        }
        .summary-container {
            display: inline-block;
            text-align: left;
            max-width: 600px;
            width: 100%;
            margin: 20px auto;
            padding: 20px;
            border: 1px solid #ddd;
            border-radius: 5px;
        }
        .summary-section {
            margin-bottom: 20px;
        }
        .summary-section h2 {
            color: #7a8c5c;
            margin-bottom: 10px;
        }
        .summary-item {
            margin-bottom: 10px;
        }
        .summary-label {
            font-weight: bold;
            color: #666;
        }
        .success-message {
            color: #28a745;
            font-size: 1.2em;
            margin: 20px 0;
        }
        .poll-url {
            background-color: #f8f9fa;
            padding: 10px;
            border-radius: 5px;
            margin: 10px 0;
            word-break: break-all;
        }
        .button {
            padding: 10px 20px;
            background-color: #7a8c5c;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
            margin-top: 20px;
        }
    </style>
</head>
<body>
    <img src="/woodle-logo.jpeg" alt="Woodle Logo" class="logo">
    <h1>Poll Summary</h1>

    <div class="summary-container">
        <div class="success-message">
            Your poll has been created successfully!
        </div>

        <div class="summary-section" data-test-section="poll-details">
            <h2>Poll Details</h2>
            <div class="summary-item" data-test-item="title">
                <span class="summary-label">Title:</span>
                <span>${step1FormData.title()}</span>
            </div>
            <div class="summary-item" data-test-item="description">
                <span class="summary-label">Description:</span>
                <span>${step1FormData.description()}</span>
            </div>
        </div>

        <div class="summary-section" data-test-section="event-details">
            <h2>Event Details</h2>
            <div class="summary-item" data-test-item="date">
                <span class="summary-label">Date:</span>
                <span>${step2FormData.date()}</span>
            </div>
            <div class="summary-item" data-test-item="time">
                <span class="summary-label">Time:</span>
                <span>${step2FormData.startTime()} - ${step2FormData.endTime()}</span>
            </div>
            <div class="summary-item" data-test-item="expiry-date">
                <span class="summary-label">Expiry Date:</span>
                <span>${step3FormData.expiryDate()}</span>
            </div>
        </div>

        <div class="summary-section" data-test-section="organizer-details">
            <h2>Organizer Details</h2>
            <div class="summary-item" data-test-item="name">
                <span class="summary-label">Name:</span>
                <span>${step1FormData.name()}</span>
            </div>
            <div class="summary-item" data-test-item="email">
                <span class="summary-label">Email:</span>
                <span>${step1FormData.email()}</span>
            </div>
        </div>

        <div class="summary-section" data-test-section="poll-url">
            <h2>Poll URL</h2>
            <div class="poll-url">
                ${pollUrl}
            </div>
        </div>

        <a href="/" class="button">Back to Home</a>
    </div>
</body>
</html> 
@param de.bas.bodo.woodle.view.ScheduleEventStep2Form step2FormData

<!DOCTYPE html>
<html>
<head>
    <title>Schedule Event - Step 2</title>
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
        form {
            display: inline-block;
            text-align: left;
            max-width: 400px;
            width: 100%;
        }
        label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
        }
        input[type="date"], input[type="time"] {
            padding: 8px;
            width: 100%;
            margin-bottom: 10px;
        }
        button {
            padding: 10px 20px;
            background-color: #7a8c5c;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            margin-right: 10px;
        }
        .input-group {
            position: relative;
            margin-bottom: 15px;
        }
    </style>
</head>
<body>
    <img src="/woodle-logo.jpeg" alt="Woodle Logo" class="logo">
    <h1>Schedule Event - Step 2</h1>
    <form action="/schedule-event-step2" method="post">
        <div class="input-group">
            <label for="date">Choose a date</label>
            <input type="date" id="date" name="date" required value="${step2FormData.date() != null ? step2FormData.date() : ""}">
        </div>

        <div class="input-group">
            <label for="startTime">Start time</label>
            <input type="time" id="startTime" name="startTime" required value="${step2FormData.startTime() != null ? step2FormData.startTime() : ""}">
        </div>

        <div class="input-group">
            <label for="endTime">End time</label>
            <input type="time" id="endTime" name="endTime" required value="${step2FormData.endTime() != null ? step2FormData.endTime() : ""}">
        </div>

        <button type="button" onclick="window.history.back()">Back</button>
        <button type="submit">Next</button>
    </form>
</body>
</html> 
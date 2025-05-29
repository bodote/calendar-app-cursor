@param de.bas.bodo.woodle.view.ScheduleEventStep3Form step3FormData

<!DOCTYPE html>
<html>
<head>
    <title>Schedule Event - Step 3</title>
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
        input[type="date"] {
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
    <h1>Schedule Event - Step 3</h1>
    <form action="/schedule-event-step3" method="post">
        <div class="input-group">
            <label for="expiryDate">Expiry Date</label>
            <input type="date" id="expiryDate" name="expiryDate" required value="${step3FormData.expiryDate() != null ? step3FormData.expiryDate() : ""}">
        </div>

        <button type="button" onclick="window.history.back()">Back</button>
        <button type="submit">Create the poll</button>
    </form>
</body>
</html> 
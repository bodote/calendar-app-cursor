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
        .add-time-slot {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 40px;
            height: 40px;
            padding: 0;
            margin: 10px auto;
            background-color: #7a8c5c;
            border-radius: 50%;
        }
        .add-time-slot img {
            width: 20px;
            height: 20px;
        }
        .time-slot {
            border: 1px solid #ddd;
            padding: 15px;
            margin-bottom: 15px;
            border-radius: 5px;
        }
    </style>
</head>
<body>
    <img src="/woodle-logo.jpeg" alt="Woodle Logo" class="logo">
    <h1>Schedule Event - Step 2</h1>
    <form action="/schedule-event-step2" method="post">
        !{int index = 0;}
        @for(int i = 0; i < step2FormData.timeSlots().size(); i++)
            !{var timeSlot = step2FormData.timeSlots().get(i);}
            <div class="time-slot">
                <div class="input-group">
                    <label for="date${i}">Date:</label>
                    <input type="date" id="date${i}" name="date${i}" data-test="date-${i}" value="${timeSlot.date()}" required>
                </div>

                <div class="input-group">
                    <label for="startTime${i}">Start Time:</label>
                    <input type="time" id="startTime${i}" name="startTime${i}" data-test="startTime-${i}" value="${timeSlot.startTime()}" required>
                </div>

                <div class="input-group">
                    <label for="endTime${i}">End Time:</label>
                    <input type="time" id="endTime${i}" name="endTime${i}" data-test="endTime-${i}" value="${timeSlot.endTime()}" required>
                </div>
            </div>
        @endfor

        <button type="button" class="add-time-slot" data-test="add-time-slot" onclick="addTimeSlot()">
            <img src="/Plus-Symbol-Transparent-small.png" alt="+">
        </button>

        <button type="button" onclick="window.history.back()">Back</button>
        <button type="submit">Next</button>
    </form>

    <script>
        function addTimeSlot() {
            const form = document.querySelector('form');
            
            // Collect ALL form data, not just the first time slot
            const formData = new FormData(form);

            fetch('/schedule-event-step2/add-time-slot', {
                method: 'POST',
                body: formData
            }).then(() => {
                window.location.reload();
            });
        }
    </script>
</body>
</html> 
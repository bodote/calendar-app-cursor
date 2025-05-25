<!DOCTYPE html>
<html>
<head>
    <title>Schedule Event</title>
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
        }
        label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
        }
        input[type="text"], input[type="email"] {
            padding: 8px;
            width: 100%;
            margin-bottom: 10px;
        }
        textarea {
            width: 100%;
            height: 120px;
            margin-bottom: 10px;
        }
        button {
            padding: 10px 20px;
            background-color: #7a8c5c;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
        }
        .optional {
            margin-top: 20px;
        }
        .optional-toggle {
            color: #007bff;
            cursor: pointer;
            text-decoration: underline;
        }
    </style>
    <script>
        function toggleOptional() {
            var section = document.getElementById('optional-section');
            if (section.style.display === 'none') {
                section.style.display = 'block';
            } else {
                section.style.display = 'none';
            }
        }
    </script>
</head>
<body>
    <img src="/woodle-logo.jpeg" alt="Woodle Logo" class="logo">
    <h1>Schedule Event</h1>
    <form action="/schedule-event" method="post">
        <label for="name">Your name *</label>
        <input type="text" id="name" name="name" required>

        <label for="email">Your email address *</label>
        <input type="email" id="email" name="email" required>

        <label for="title">Poll title *</label>
        <input type="text" id="title" name="title" required>

        <label for="description">Description</label>
        <textarea id="description" name="description"></textarea>

        <div class="optional">
            <span class="optional-toggle" onclick="toggleOptional()">Optional parameters &#9660;</span>
            <div id="optional-section" style="display:none;">
                <!-- Placeholder for optional parameters -->
            </div>
        </div>

        <button type="submit">Go to step 2</button>
    </form>
</body>
</html> 
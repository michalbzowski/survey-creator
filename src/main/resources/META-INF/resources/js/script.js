document.getElementById('survey-form').addEventListener('submit', submitSurvey);

async function submitSurvey(event) {
    event.preventDefault();

    const name = document.getElementById('name').value;

    const questions = [];
    document.querySelectorAll('.question-entry').forEach(div => {
        questions.push({
            title: div.querySelector('input[name$=".title"]').value,
            description: div.querySelector('input[name$=".description"]').value,
        });
    });

    const survey = {
        name,
        questions
    };

    const response = await fetch('/web/surveys', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(survey),
    });

    if (response.ok) {
        window.location.href = '/web/surveys'; // przekierowanie po sukcesie
    } else {
        alert('Błąd przy zapisie ankiety');
    }
}

function addQuestion() {
      const container = document.getElementById('questions-container');
      const count = container.children.length;

      const div = document.createElement('div');
      div.className = 'question-entry';
      div.innerHTML = `
          <label> Tytuł pytania: </label>
          <input type="text" name="questions[${count}].title" value="" />
          <label> Opis pytania: </label>
          <input type="text" name="questions[${count}].description" value="" />
          <button type="button" onclick="removeQuestion(this)">Usuń pytanie</button>
      `;
      container.appendChild(div);
    }

    function removeQuestion(button) {
      const entry = button.parentNode;
      entry.parentNode.removeChild(entry);

      // Po usunięciu, zaktualizuj indeksy name w polach input
      const container = document.getElementById('questions-container');
      Array.from(container.children).forEach((child, index) => {
          child.querySelectorAll('input').forEach(input => {
              if(input.name.includes('title')) {
                  input.name = `questions[${index}].title`;
              } else if(input.name.includes('description')) {
                  input.name = `questions[${index}].description`;
              }
          });
      });
    }
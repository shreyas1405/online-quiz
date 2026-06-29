document.addEventListener('DOMContentLoaded', () => {
    const timerDisplay = document.getElementById('timer');
    const form = document.getElementById('quiz-form');
    if (!timerDisplay || !form) return;

    let duration = parseInt(timerDisplay.dataset.duration);
    
    function updateTimer() {
        if (duration <= 0) {
            timerDisplay.innerHTML = "00:00";
            timerDisplay.style.color = "var(--danger)";
            
            // Auto submit
            const timedOutInput = document.createElement('input');
            timedOutInput.type = 'hidden';
            timedOutInput.name = 'timedOut';
            timedOutInput.value = 'true';
            form.appendChild(timedOutInput);
            form.submit();
            return;
        }

        const minutes = Math.floor(duration / 60);
        const seconds = duration % 60;
        
        timerDisplay.innerHTML = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        
        if (duration <= 60) {
            timerDisplay.style.color = "var(--danger)";
            timerDisplay.style.animation = "pulse 1s infinite";
        }
        
        duration--;
        setTimeout(updateTimer, 1000);
    }
    
    updateTimer();
});

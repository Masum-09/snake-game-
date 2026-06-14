# 🐍 Snake Game — Java

A feature-rich Snake game built with **Java Swing** following **OOP principles**. Minimalist dark UI with multiple food types, power-ups, leveling system, and adaptive speed.

---

## 📸 Screenshot

<!-- Add a screenshot of the game here -->
<!-- Example: ![Gameplay Screenshot](screenshots/gameplay.png) -->

<img width="832" height="745" alt="Screenshot 2026-06-14 175126" src="https://github.com/user-attachments/assets/83372438-3b57-4924-af25-b4348b7fcf91" />
<img width="837" height="737" alt="Screenshot 2026-06-14 175108" src="https://github.com/user-attachments/assets/06bb936e-9d9b-419d-979f-86a1ee7727f2" />
<img width="840" height="746" alt="Screenshot 2026-06-14 175201" src="https://github.com/user-attachments/assets/d08ac02e-5d6e-48d7-b863-5ca1e4cad8cb" />

---

## 🎬 Demo

<!-- Add a gameplay video or GIF here -->
<!-- Option 1 – GIF: ![Gameplay Demo](screenshots/demo.gif) -->
<!-- Option 2 – YouTube: [![Watch Demo](https://img.youtube.com/vi/YOUR_VIDEO_ID/0.jpg)](https://www.youtube.com/watch?v=YOUR_VIDEO_ID) -->



https://github.com/user-attachments/assets/f0551340-00d1-443b-bfd4-2d2c1d956605



---

## ✨ Features

| Feature | Description |
|---|---|
| 🟢 Normal Food | +10 points, always present |
| 🟡 Bonus Food | +30 points, spawns every 3–5 eats |
| 🔵 Speed Food | +5 points, temporary speed boost |
| 🟣 Slow Power-Up | Slows the snake for 4 seconds |
| 🔴 Shrink Power-Up | Removes 4 tail segments instantly |
| 📈 Leveling System | Speed increases every 5 food eaten |
| 🏆 High Score | Persists across restarts in the same session |
| ⏸ Pause / Resume | Pause anytime mid-game |
| 💀 Game Over Screen | Shows final score with restart option |

---

## 🏗️ OOP Structure

```
SnakeGame.java
│
├── enum  Direction          — UP / DOWN / LEFT / RIGHT with dx, dy vectors
├── class Point              — Grid coordinate with equals/hashCode
│
├── abstract class Food      — Base: position, value, color, draw()
│   ├── class NormalFood     — Green circle  (+10)
│   ├── class BonusFood      — Yellow triangle (+30)
│   └── class SpeedFood      — Blue circle   (+5)
│
├── class PowerUp            — Time-limited power-up (SLOW / SHRINK) with fade-out
├── class Snake              — Body deque, movement, self-collision, shrink
├── class GameBoard          — Grid dimensions, bounds check, free point finder
├── class ScoreBoard         — Score, high score, level, tick-rate formula
├── enum  GameState          — MENU / PLAYING / PAUSED / GAME_OVER
│
├── class Game               — Controller: owns all entities, drives tick logic
├── class GameRenderer       — Pure view: draws HUD, board, overlays (extends JPanel)
└── class SnakeGame          — JFrame entry point, keyboard input, adaptive timer
```

---

## 🎮 Controls

| Key | Action |
|---|---|
| `↑ W` | Move Up |
| `↓ S` | Move Down |
| `← A` | Move Left |
| `→ D` | Move Right |
| `P` | Pause / Resume |
| `R` | Restart |
| `Enter` | Start (from menu) |

---

## 🚀 Getting Started

### Prerequisites

- Java **17 or higher** (uses pattern matching in switch expressions)

### Run

```bash
# Clone the repo
git clone https://github.com/YOUR_USERNAME/snake-game-java.git
cd snake-game-java

# Compile
javac SnakeGame.java

# Run
java SnakeGame
```

---

## 📁 Project Structure

```
snake-game-java/
├── SnakeGame.java      # All source code (single-file project)<img width="832" height="745" alt="Screenshot 2026-06-14 175126" src="https://github.com/user-attachments/assets/3c3e40e7-b927-4b20-8ecd-f97b86a84ce1" />
<img width="832" height="745" alt="Screenshot 2026-06-14 175126" src="https://github.com/user-attachments/assets/e8e8674c-9ca3-49d2-92da-5518f17fc1f9" />

└── README.md
```

---

## 🛠️ Built With

- **Java 17+**
- **Java Swing** — UI & rendering
- **Java AWT** — Graphics2D, keyboard input

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

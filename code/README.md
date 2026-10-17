# Ping Pop 3D - Table Tennis (Java Swing, pseudo-3D perspective)

First-person view from behind your red racquet, glossy blue table, net, wooden floor, red slatted wall.
Pure Java 2D - no external libraries.

## Compile & run (JDK 17+)

    cd code
    mkdir bin
    javac -d bin src/tabletennis/*.java
    java -cp bin tabletennis.Main

## Controls
- MOUSE: your racquet follows the cursor (left/right + up/down). Hold the button to "grab" it (white ring).
- CLICK or SPACE: serve.  P: pause.  ESC: menu.  Menu: click the chips, or 1/2/3, LEFT/RIGHT, M.
- Keyboard alternative: W A S D (or the arrow keys) move the racquet.
- Modes: VS COMPUTER (Easy / Medium / Hard) or 2 PLAYERS (P1 mouse or WASD + SPACE, P2 arrow keys + ENTER).
- Rules: ball must bounce once on the opponent's side, clear the net, and stay on the table. First to 7 wins.

## OOP concepts used
- Abstraction: interfaces `Renderable`, `GameEntity`; abstract classes `GameObject3D`, `Racquet`
- Inheritance: `Racquet` -> `PlayerRacquet`, `AIRacquet`; `Ball`, `Racquet` extend `GameObject3D`
- Polymorphism: `GamePanel` holds `Racquet nearR, farR` - each can be a human or the AI; `targetX()`,
  `chooseShot()` are overridden; every entity is drawn through `Renderable.draw(g, camera)`
- Template-method pattern: `Racquet.update()` is fixed, subclasses supply `targetX/targetY/maxSpeed`
- Encapsulation: private state + getters/setters (`Scoreboard`, `Camera`, `Ball`, `Racquet`)
- Composition: `GamePanel` owns Room, Table, Net, Ball, Racquets, Scoreboard, ParticleSystem
- Enums with fields/methods: `Difficulty`, `GameMode`, `GameState`
- Record (`Racquet.Shot`), inner classes (`KeyHandler`, `MouseHandler`), static utility classes
  (`Theme`, `Court`, `ShotCalculator`), generic collections (`List<Particle>`, `Deque<double[]>`)

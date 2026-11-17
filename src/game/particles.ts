export interface Particle {
  x: number;
  y: number;
  vx: number;
  vy: number;
  size: number;
  color: string;
  rotation: number;
  vr: number;
  life: number;
  maxLife: number;
  kind: 'confetti' | 'sparkle' | 'star';
  gravity: number;
}

const PASTEL_CONFETTI_COLORS = ['#FF8EC4', '#FFE07A', '#94E0FF', '#CFA8FF', '#A5EB9E', '#FFB997'];

export class ParticleSystem {
  private particles: Particle[] = [];

  clear(): void {
    this.particles = [];
  }

  emitSnapSparkles(cx: number, cy: number, radius = 42): void {
    const count = 16;
    for (let i = 0; i < count; i++) {
      const angle = (i / count) * Math.PI * 2 + (Math.random() - 0.5) * 0.35;
      const speed = 70 + Math.random() * 130;
      const color = PASTEL_CONFETTI_COLORS[i % PASTEL_CONFETTI_COLORS.length]!;
      this.particles.push({
        x: cx + Math.cos(angle) * radius * 0.25,
        y: cy + Math.sin(angle) * radius * 0.25,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed - 25,
        size: 6 + Math.random() * 6,
        color,
        rotation: Math.random() * Math.PI * 2,
        vr: (Math.random() - 0.5) * 6,
        life: 0,
        maxLife: 0.55 + Math.random() * 0.3,
        kind: i % 3 === 0 ? 'star' : 'sparkle',
        gravity: 120
      });
    }
  }

  emitCelebrationFireworks(width: number, height: number): void {
    const origins = [
      { x: width * 0.24, y: height * 0.36 },
      { x: width * 0.76, y: height * 0.34 },
      { x: width * 0.5, y: height * 0.26 }
    ];

    for (const origin of origins) {
      for (let i = 0; i < 34; i++) {
        const angle = -Math.PI * 0.5 + (Math.random() - 0.5) * Math.PI * 1.45;
        const speed = 130 + Math.random() * 270;
        const color = PASTEL_CONFETTI_COLORS[i % PASTEL_CONFETTI_COLORS.length]!;
        const kind: Particle['kind'] = i % 5 === 0 ? 'star' : 'confetti';
        this.particles.push({
          x: origin.x + (Math.random() - 0.5) * 24,
          y: origin.y + (Math.random() - 0.5) * 20,
          vx: Math.cos(angle) * speed,
          vy: Math.sin(angle) * speed - 60,
          size: kind === 'star' ? 9 + Math.random() * 6 : 7 + Math.random() * 6,
          color,
          rotation: Math.random() * Math.PI * 2,
          vr: (Math.random() - 0.5) * 9,
          life: 0,
          maxLife: 1.7 + Math.random() * 1.1,
          kind,
          gravity: 240
        });
      }
    }
  }

  update(dt: number): void {
    const clampedDt = Math.min(0.05, Math.max(0.001, dt));
    for (let i = this.particles.length - 1; i >= 0; i--) {
      const p = this.particles[i]!;
      p.life += clampedDt;
      if (p.life >= p.maxLife) {
        this.particles.splice(i, 1);
        continue;
      }
      // Air drag + soft paper flutter
      p.vx *= Math.pow(0.45, clampedDt);
      p.vy += p.gravity * clampedDt;
      p.x += p.vx * clampedDt;
      p.y += p.vy * clampedDt;
      p.rotation += p.vr * clampedDt;
    }
  }

  draw(ctx: CanvasRenderingContext2D): void {
    for (const p of this.particles) {
      const progress = p.life / p.maxLife;
      const alpha = progress < 0.7 ? 1 : 1 - (progress - 0.7) / 0.3;

      ctx.save();
      ctx.globalAlpha = Math.max(0, alpha);
      ctx.translate(p.x, p.y);
      ctx.rotate(p.rotation);
      ctx.fillStyle = p.color;

      if (p.kind === 'confetti') {
        const tilt = Math.cos(p.life * 9 + p.rotation);
        ctx.scale(1, Math.max(0.25, Math.abs(tilt)));
        ctx.beginPath();
        ctx.roundRect(-p.size * 0.65, -p.size * 0.35, p.size * 1.3, p.size * 0.7, 2);
        ctx.fill();
      } else if (p.kind === 'star') {
        ctx.beginPath();
        for (let k = 0; k < 10; k++) {
          const r = k % 2 === 0 ? p.size : p.size * 0.45;
          const a = (k * Math.PI) / 5 - Math.PI / 2;
          if (k === 0) ctx.moveTo(Math.cos(a) * r, Math.sin(a) * r);
          else ctx.lineTo(Math.cos(a) * r, Math.sin(a) * r);
        }
        ctx.closePath();
        ctx.fill();
      } else {
        ctx.beginPath();
        ctx.moveTo(0, -p.size);
        ctx.quadraticCurveTo(p.size * 0.2, -p.size * 0.2, p.size, 0);
        ctx.quadraticCurveTo(p.size * 0.2, p.size * 0.2, 0, p.size);
        ctx.quadraticCurveTo(-p.size * 0.2, p.size * 0.2, -p.size, 0);
        ctx.quadraticCurveTo(-p.size * 0.2, -p.size * 0.2, 0, -p.size);
        ctx.closePath();
        ctx.fill();
      }

      ctx.restore();
    }
  }
}

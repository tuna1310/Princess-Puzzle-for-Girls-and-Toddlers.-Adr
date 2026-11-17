/**
 * Procedural WebAudio synthesizer for gentle, zero-punishment toddler sound effects.
 * Zero external audio assets required; respects user mute setting and browser autoplay locks.
 */
export class SoundEngine {
  private ctx: AudioContext | null = null;
  private enabled = true;

  isEnabled(): boolean {
    return this.enabled;
  }

  setEnabled(enabled: boolean): void {
    this.enabled = enabled;
  }

  /**
   * Unlock or create the AudioContext inside a user gesture handler.
   */
  unlock(): void {
    if (typeof window === 'undefined') {
      return;
    }
    const AudioCtx =
      window.AudioContext ||
      (window as unknown as { webkitAudioContext?: typeof AudioContext }).webkitAudioContext;
    if (!AudioCtx) {
      return;
    }
    if (!this.ctx) {
      this.ctx = new AudioCtx();
    }
    if (this.ctx.state === 'suspended') {
      void this.ctx.resume().catch(() => {
        // Ignore resume failure prior to user gesture
      });
    }
  }

  private readyContext(): AudioContext | null {
    if (!this.enabled) {
      return null;
    }
    this.unlock();
    if (!this.ctx || this.ctx.state === 'closed') {
      return null;
    }
    return this.ctx;
  }

  private playTone(
    freqStart: number,
    freqEnd: number,
    duration: number,
    type: OscillatorType,
    peakGain: number,
    delay = 0
  ): void {
    const ctx = this.readyContext();
    if (!ctx) {
      return;
    }
    const now = ctx.currentTime + delay;
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();

    osc.type = type;
    osc.frequency.setValueAtTime(freqStart, now);
    if (freqEnd !== freqStart) {
      osc.frequency.exponentialRampToValueAtTime(Math.max(40, freqEnd), now + duration);
    }

    gain.gain.setValueAtTime(0.0001, now);
    gain.gain.linearRampToValueAtTime(peakGain, now + Math.min(0.025, duration * 0.25));
    gain.gain.exponentialRampToValueAtTime(0.0001, now + duration);

    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start(now);
    osc.stop(now + duration + 0.02);
  }

  /**
   * Soft wooden marimba pluck when picking up a puzzle piece.
   */
  playPickup(): void {
    this.playTone(440, 520, 0.11, 'sine', 0.14);
    this.playTone(880, 1040, 0.07, 'triangle', 0.04);
  }

  /**
   * Gentle playful bubble bounce when a piece floats back to the tray.
   * Intentionally warm and non-punitive.
   */
  playReturnBounce(): void {
    this.playTone(340, 260, 0.12, 'sine', 0.08);
    this.playTone(270, 390, 0.14, 'sine', 0.07, 0.07);
  }

  /**
   * Sparkling pentatonic chime when a piece snaps cleanly into place.
   */
  playSnap(placedCount: number, totalCount: number): void {
    const scale = [523.25, 587.33, 659.25, 783.99, 880.0, 1046.5];
    const ratio = totalCount > 1 ? (placedCount - 1) / (totalCount - 1) : 0;
    const index = Math.min(scale.length - 1, Math.floor(ratio * (scale.length - 1)));
    const root = scale[index] ?? 523.25;

    this.playTone(root, root * 1.01, 0.26, 'sine', 0.16);
    this.playTone(root * 1.5, root * 1.5, 0.24, 'triangle', 0.07, 0.035);
    this.playTone(root * 2, root * 2, 0.22, 'sine', 0.05, 0.065);
  }

  /**
   * Gentle storybook tap for UI buttons and wheel nudges.
   */
  playTap(): void {
    this.playTone(580, 620, 0.07, 'sine', 0.09);
  }

  /**
   * Joyful harp & celesta fanfare when a puzzle is completed.
   */
  playWinFanfare(): void {
    const notes = [523.25, 659.25, 783.99, 987.77, 1046.5, 1318.5, 1567.98];
    notes.forEach((freq, i) => {
      const delay = i * 0.075;
      this.playTone(freq, freq, 0.42, 'sine', 0.13, delay);
      this.playTone(freq * 2, freq * 2, 0.28, 'triangle', 0.04, delay + 0.015);
    });
  }
}

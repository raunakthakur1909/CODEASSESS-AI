interface ComplexityCardProps {
  label: string;
  value: string;
  accent: 'lime' | 'coral';
}

function complexityLabel(value: string): string {
  const normalized = value.trim().toUpperCase();
  if (normalized === 'O(1)') return 'Constant';
  if (normalized.includes('O(LOG')) return 'Logarithmic';
  if (normalized === 'O(N)') return 'Linear';
  if (normalized.includes('O(N LOG')) return 'Quasilinear';
  if (normalized.includes('O(N^2)') || normalized.includes('O(N²)')) return 'Quadratic';
  return 'Review notation';
}

function ComplexityCard({ label, value, accent }: ComplexityCardProps) {
  const displayValue = value || 'Not available';

  return (
    <article className={`complexity-card complexity-card--${accent}`}>
      <div>
        <span className="complexity-label">{label}</span>
        <strong className="complexity-meaning">{complexityLabel(displayValue)}</strong>
      </div>
      <code>{displayValue}</code>
    </article>
  );
}

export default ComplexityCard;

interface Props {
  value: string;
}

export function StatusBadge({ value }: Props) {
  const cls = `badge badge-${value.toLowerCase().replace("_", "-")}`;
  return <span className={cls}>{value.replace("_", " ")}</span>;
}

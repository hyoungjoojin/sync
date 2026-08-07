'use client';

import { useState } from 'react';

import { Input } from '@/components/ui/input';

interface PhoneNumberInputProps {
  id: string;
  value: string;
  onChange: (value: string) => void;
  onBlur: () => void;
  'aria-invalid'?: boolean;
}

function splitDigits(value: string): [string, string, string] {
  if (value.length <= 3) {
    return [value, '', ''];
  }
  if (value.length <= 7) {
    return [value.slice(0, 3), value.slice(3), ''];
  }
  return [value.slice(0, 3), value.slice(3, -4), value.slice(-4)];
}

const onlyDigits = (value: string) => value.replace(/\D/g, '');

export default function PhoneNumberInput({
  id,
  value,
  onChange,
  onBlur,
  'aria-invalid': ariaInvalid,
}: PhoneNumberInputProps) {
  const [parts, setParts] = useState<[string, string, string]>(() =>
    splitDigits(value),
  );

  const updatePart = (index: 0 | 1 | 2, raw: string, maxLength: number) => {
    const digits = onlyDigits(raw).slice(0, maxLength);
    const next: [string, string, string] = [...parts];
    next[index] = digits;
    setParts(next);
    onChange(next.join(''));
  };

  return (
    <div className="flex items-center gap-2">
      <Input
        id={id}
        value={parts[0]}
        onChange={(event) => updatePart(0, event.target.value, 3)}
        onBlur={onBlur}
        aria-invalid={ariaInvalid}
        inputMode="numeric"
        maxLength={3}
        className="w-16 text-center"
      />
      <span className="text-muted-foreground">-</span>
      <Input
        value={parts[1]}
        onChange={(event) => updatePart(1, event.target.value, 4)}
        onBlur={onBlur}
        aria-invalid={ariaInvalid}
        inputMode="numeric"
        maxLength={4}
        className="w-20 text-center"
      />
      <span className="text-muted-foreground">-</span>
      <Input
        value={parts[2]}
        onChange={(event) => updatePart(2, event.target.value, 4)}
        onBlur={onBlur}
        aria-invalid={ariaInvalid}
        inputMode="numeric"
        maxLength={4}
        className="w-20 text-center"
      />
    </div>
  );
}

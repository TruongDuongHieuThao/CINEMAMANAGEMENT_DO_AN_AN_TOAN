"use client";

import { useState } from "react";
import { Minus, Plus } from "lucide-react";
import type { ComboItem } from "@/lib/types";

interface ComboSelectionStepProps {
  combos: ComboItem[];
  selectedCombos: ComboItem[];
  onSelectCombos: (combos: ComboItem[]) => void;
}

export default function ComboSelectionStep({
  combos,
  selectedCombos,
  onSelectCombos,
}: ComboSelectionStepProps) {
  const [quantities, setQuantities] = useState<{ [key: string]: number }>({});

  const getQuantity = (comboId: string): number => {
    return quantities[comboId] || 1;
  };

  const handleAddCombo = (combo: ComboItem) => {
    const isSelected = selectedCombos.some((c) => c.id === combo.id);
    if (!isSelected) {
      setQuantities({ ...quantities, [combo.id]: 1 });
      onSelectCombos([...selectedCombos, { ...combo, quantity: 1 }]);
    }
  };

  const handleRemoveCombo = (comboId: string) => {
    onSelectCombos(selectedCombos.filter((c) => c.id !== comboId));
    const newQuantities = { ...quantities };
    delete newQuantities[comboId];
    setQuantities(newQuantities);
  };

  const handleQuantityChange = (comboId: string, quantity: number) => {
    if (quantity <= 0) {
      handleRemoveCombo(comboId);
      return;
    }
    setQuantities({ ...quantities, [comboId]: quantity });
    // Update quantity in selectedCombos
    onSelectCombos(
      selectedCombos.map((combo) =>
        combo.id === comboId ? { ...combo, quantity } : combo,
      ),
    );
  };

  const handleComboClick = (combo: ComboItem) => {
    const isSelected = selectedCombos.some((c) => c.id === combo.id);
    if (isSelected) {
      handleRemoveCombo(combo.id);
    } else {
      handleAddCombo(combo);
    }
  };

  return (
    <div className="bg-card dark:bg-slate-900 border border-border dark:border-slate-800 rounded-xl p-8">
      <h2 className="text-2xl font-bold mb-2">Choose Your Combos</h2>
      <p className="text-muted-foreground mb-8">
        Add snacks and drinks to your booking
      </p>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {combos.map((combo) => {
          const isSelected = selectedCombos.some((c) => c.id === combo.id);
          const quantity = getQuantity(combo.id);

          return (
            <div
              key={combo.id}
              onClick={() => handleComboClick(combo)}
              className={`p-5 rounded-lg border-2 transition-all text-left flex gap-4 cursor-pointer ${
                isSelected
                  ? "border-purple-600 bg-purple-500/10 dark:bg-purple-900/20"
                  : "border-border dark:border-slate-800 bg-muted dark:bg-slate-800 hover:border-purple-600 hover:shadow-md"
              }`}
            >
              <div className="w-24 h-24 rounded-lg overflow-hidden bg-gradient-to-br from-purple-500/20 to-purple-700/10 flex items-center justify-center shrink-0">
                {combo.imageUrl ? (
                  <img
                    src={combo.imageUrl}
                    alt={combo.name}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <span className="text-3xl">{combo.icon}</span>
                )}
              </div>

              <div className="flex-1">
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <h3 className="font-bold mb-1">{combo.name}</h3>
                    <p className="text-lg font-semibold text-purple-600">
                      {combo.price.toLocaleString()} VND
                    </p>
                  </div>
                </div>

                {combo.description && (
                  <p className="text-sm text-muted-foreground mt-2 line-clamp-2">
                    {combo.description}
                  </p>
                )}

                {combo.items && combo.items.length > 0 && (
                  <ul className="mt-3 space-y-1 text-sm text-muted-foreground">
                    {combo.items.map((item) => (
                      <li
                        key={item.id}
                        className="flex items-center justify-between"
                      >
                        <span>{item.name}</span>
                        <span className="font-semibold text-foreground">
                          x{item.quantity}
                        </span>
                      </li>
                    ))}
                  </ul>
                )}

                {isSelected && (
                  <div
                    className="mt-4 flex items-center gap-2"
                    onClick={(e) => e.stopPropagation()}
                  >
                    <button
                      onClick={() =>
                        handleQuantityChange(combo.id, quantity - 1)
                      }
                      className="w-8 h-8 rounded border border-purple-600 flex items-center justify-center hover:bg-purple-500/20 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                      <Minus size={16} className="text-purple-600" />
                    </button>
                    <input
                      type="number"
                      min="0"
                      value={quantity}
                      onChange={(e) => {
                        const value = parseInt(e.target.value, 10);
                        if (!isNaN(value) && value >= 0) {
                          handleQuantityChange(combo.id, value);
                        }
                      }}
                      className="w-12 h-8 border border-purple-600 rounded text-center font-semibold bg-background dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-purple-600"
                    />
                    <button
                      onClick={() =>
                        handleQuantityChange(combo.id, quantity + 1)
                      }
                      className="w-8 h-8 rounded border border-purple-600 flex items-center justify-center hover:bg-purple-500/20 transition-colors"
                    >
                      <Plus size={16} className="text-purple-600" />
                    </button>
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {selectedCombos.length > 0 && (
        <div className="mt-8 p-4 bg-purple-500/10 dark:bg-purple-900/20 border border-purple-500/30 rounded-lg">
          <p className="text-sm font-semibold mb-3">Selected Combos:</p>
          <ul className="text-sm space-y-2">
            {selectedCombos.map((combo) => {
              const comboQuantity = combo.quantity || getQuantity(combo.id);
              return (
                <li
                  key={combo.id}
                  className="flex items-center justify-between"
                >
                  <span>
                    {combo.name} x{comboQuantity} -{" "}
                    {(combo.price * comboQuantity).toLocaleString()} VND
                  </span>
                </li>
              );
            })}
          </ul>
        </div>
      )}
    </div>
  );
}

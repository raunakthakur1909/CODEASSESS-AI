import React, { useState, useRef, useEffect } from 'react';
import { Bell, CheckCheck, Trash2, Sparkles, CheckCircle2, AlertCircle, Info } from 'lucide-react';

export interface NotificationItem {
  id: string;
  title: string;
  message: string;
  time: string;
  read: boolean;
  type: 'success' | 'info' | 'warning' | 'review';
}

const INITIAL_NOTIFICATIONS: NotificationItem[] = [
  {
    id: '1',
    title: 'AI Code Review Ready',
    message: 'Your recent submission was evaluated by Claude 3.5 Sonnet with detailed complexity insights.',
    time: '10m ago',
    read: false,
    type: 'review',
  },
  {
    id: '2',
    title: 'Assessment In Progress',
    message: 'Backend Developer — Java & Algorithms attempt is currently active.',
    time: '45m ago',
    read: false,
    type: 'info',
  },
  {
    id: '3',
    title: 'System Healthy',
    message: 'Database connection pool and sandbox runners operating at <10ms latency.',
    time: '2h ago',
    read: true,
    type: 'success',
  },
  {
    id: '4',
    title: 'Welcome to CodeAssess AI',
    message: 'Explore algorithms, test your code, and receive real-time automated scoring.',
    time: '1d ago',
    read: true,
    type: 'info',
  },
];

export const NotificationsDropdown: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [notifications, setNotifications] = useState<NotificationItem[]>(INITIAL_NOTIFICATIONS);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const unreadCount = notifications.filter(n => !n.read).length;

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };
    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen]);

  const markAllAsRead = () => {
    setNotifications(prev => prev.map(n => ({ ...n, read: true })));
  };

  const clearAll = () => {
    setNotifications([]);
  };

  const toggleRead = (id: string) => {
    setNotifications(prev =>
      prev.map(n => (n.id === id ? { ...n, read: true } : n))
    );
  };

  const getIcon = (type: NotificationItem['type']) => {
    switch (type) {
      case 'review':
        return <Sparkles size={16} className="text-primary" />;
      case 'success':
        return <CheckCircle2 size={16} style={{ color: 'hsl(var(--primary))' }} />;
      case 'warning':
        return <AlertCircle size={16} style={{ color: 'hsl(var(--danger))' }} />;
      default:
        return <Info size={16} style={{ color: 'hsl(var(--text-secondary))' }} />;
    }
  };

  return (
    <div className="relative" ref={dropdownRef} style={{ position: 'relative' }}>
      <button
        type="button"
        id="notifications-btn"
        className="icon-button"
        onClick={() => setIsOpen(!isOpen)}
        aria-label="Notifications"
        title="Notifications"
        style={{ position: 'relative' }}
      >
        <Bell size={17} />
        {unreadCount > 0 && (
          <span
            style={{
              position: 'absolute',
              top: '6px',
              right: '6px',
              width: '8px',
              height: '8px',
              background: '#ef4444',
              borderRadius: '50%',
              border: '2px solid hsl(var(--card))',
            }}
          />
        )}
      </button>

      {isOpen && (
        <div
          id="notifications-panel"
          style={{
            position: 'absolute',
            top: 'calc(100% + 8px)',
            right: 0,
            width: '340px',
            maxWidth: '90vw',
            background: 'hsl(var(--card))',
            border: '1px solid hsl(var(--border))',
            borderRadius: '12px',
            boxShadow: '0 12px 32px rgba(0, 0, 0, 0.25)',
            zIndex: 100,
            overflow: 'hidden',
            display: 'flex',
            flexDirection: 'column',
          }}
        >
          {/* Header */}
          <div
            style={{
              padding: '12px 16px',
              borderBottom: '1px solid hsl(var(--border))',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span style={{ fontWeight: 700, fontSize: '14px', color: 'hsl(var(--text-primary))' }}>
                Notifications
              </span>
              {unreadCount > 0 && (
                <span
                  style={{
                    fontSize: '11px',
                    fontWeight: 700,
                    padding: '2px 6px',
                    borderRadius: '999px',
                    background: 'hsl(var(--primary-light))',
                    color: 'hsl(var(--primary))',
                  }}
                >
                  {unreadCount} new
                </span>
              )}
            </div>
            {unreadCount > 0 && (
              <button
                type="button"
                onClick={markAllAsRead}
                style={{
                  background: 'none',
                  border: 'none',
                  color: 'hsl(var(--primary))',
                  cursor: 'pointer',
                  fontSize: '11px',
                  fontWeight: 600,
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px',
                  padding: 0,
                }}
                title="Mark all as read"
              >
                <CheckCheck size={14} />
                Mark all read
              </button>
            )}
          </div>

          {/* List */}
          <div
            style={{
              maxHeight: '340px',
              overflowY: 'auto',
              display: 'flex',
              flexDirection: 'column',
            }}
          >
            {notifications.length === 0 ? (
              <div
                style={{
                  padding: '32px 16px',
                  textAlign: 'center',
                  color: 'hsl(var(--text-muted))',
                  fontSize: '13px',
                }}
              >
                No notifications right now.
              </div>
            ) : (
              notifications.map(item => (
                <div
                  key={item.id}
                  onClick={() => toggleRead(item.id)}
                  style={{
                    padding: '12px 16px',
                    borderBottom: '1px solid hsl(var(--border))',
                    display: 'flex',
                    gap: '12px',
                    alignItems: 'flex-start',
                    cursor: 'pointer',
                    background: item.read ? 'transparent' : 'rgba(41, 172, 132, 0.05)',
                    transition: 'background 0.15s ease',
                  }}
                  onMouseEnter={e => {
                    e.currentTarget.style.background = 'rgba(41, 172, 132, 0.1)';
                  }}
                  onMouseLeave={e => {
                    e.currentTarget.style.background = item.read
                      ? 'transparent'
                      : 'rgba(41, 172, 132, 0.05)';
                  }}
                >
                  <div style={{ marginTop: '2px', flexShrink: 0 }}>{getIcon(item.type)}</div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'baseline',
                        gap: '8px',
                        marginBottom: '2px',
                      }}
                    >
                      <span
                        style={{
                          fontSize: '13px',
                          fontWeight: item.read ? 600 : 700,
                          color: 'hsl(var(--text-primary))',
                        }}
                      >
                        {item.title}
                      </span>
                      <span
                        style={{
                          fontSize: '10px',
                          color: 'hsl(var(--text-muted))',
                          fontFamily: 'var(--mono)',
                          flexShrink: 0,
                        }}
                      >
                        {item.time}
                      </span>
                    </div>
                    <p
                      style={{
                        margin: 0,
                        fontSize: '12px',
                        color: 'hsl(var(--text-secondary))',
                        lineHeight: 1.45,
                      }}
                    >
                      {item.message}
                    </p>
                  </div>
                  {!item.read && (
                    <span
                      style={{
                        width: '6px',
                        height: '6px',
                        borderRadius: '50%',
                        background: 'hsl(var(--primary))',
                        marginTop: '6px',
                        flexShrink: 0,
                      }}
                    />
                  )}
                </div>
              ))
            )}
          </div>

          {/* Footer */}
          {notifications.length > 0 && (
            <div
              style={{
                padding: '8px 16px',
                borderTop: '1px solid hsl(var(--border))',
                background: 'rgba(0, 0, 0, 0.03)',
                display: 'flex',
                justifyContent: 'flex-end',
              }}
            >
              <button
                type="button"
                onClick={clearAll}
                style={{
                  background: 'none',
                  border: 'none',
                  color: 'hsl(var(--text-muted))',
                  cursor: 'pointer',
                  fontSize: '11px',
                  fontWeight: 600,
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px',
                  padding: '4px 6px',
                }}
                onMouseEnter={e => {
                  e.currentTarget.style.color = 'hsl(var(--danger))';
                }}
                onMouseLeave={e => {
                  e.currentTarget.style.color = 'hsl(var(--text-muted))';
                }}
                title="Clear all notifications"
              >
                <Trash2 size={12} />
                Clear all
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

import { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { ArrowLeft, Plus, Edit, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { PageHeader } from "@/components/ui/PageHeader";
import { ROUTES } from "@/constants/routes";
import { CinemaSelector } from "@/components/showtimes/CinemaSelector";
import { RoomFilter } from "@/components/showtimes/RoomFilter";
import { ShowtimeCalendar } from "@/components/showtimes/ShowtimeCalendar";
import { ShowtimeDetailModal } from "@/components/showtimes/ShowtimeDetailModal";
import { CreateShowtimeDialog } from "@/components/showtimes/CreateShowtimeDialog";
import { EditShowtimeDialog } from "@/components/showtimes/EditShowtimeDialog";
import { DeleteShowtimeDialog } from "@/components/showtimes/DeleteShowtimeDialog";
import { getAllCinemas, type Cinema } from "@/services/cinemaService";
import {
  getAllShowtimes,
  type ShowtimeResponse,
} from "@/services/showtimeService";
import { getRoomsByCinema, type Room } from "@/services/roomService";
import { useNotificationStore } from "@/stores";

export function ShowtimeCalendarPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const addNotification = useNotificationStore(
    (state) => state.addNotification,
  );

  // State
  const [cinemas, setCinemas] = useState<Cinema[]>([]);
  const [selectedCinemaId, setSelectedCinemaId] = useState<string | null>(
    searchParams.get("cinema") || null,
  );
  const [rooms, setRooms] = useState<Room[]>([]);
  const [selectedRoomId, setSelectedRoomId] = useState<string | null>(
    searchParams.get("room") || null,
  );
  const [allShowtimes, setAllShowtimes] = useState<ShowtimeResponse[]>([]);
  const [currentMonth, setCurrentMonth] = useState<Date>(new Date());
  const [selectedShowtimeId, setSelectedShowtimeId] = useState<string | null>(
    null,
  );
  const [detailModalOpen, setDetailModalOpen] = useState(false);

  // Dialog states
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  // Loading states
  const [loadingCinemas, setLoadingCinemas] = useState(true);
  const [loadingShowtimes, setLoadingShowtimes] = useState(false);
  const [loadingRooms, setLoadingRooms] = useState(false);

  // Load cinemas on mount
  useEffect(() => {
    loadCinemas();
  }, []);

  // Load rooms when cinema is selected
  useEffect(() => {
    if (selectedCinemaId) {
      loadRooms(selectedCinemaId);
      loadShowtimes();
    } else {
      setRooms([]);
      setSelectedRoomId(null);
    }
  }, [selectedCinemaId]);

  // Reload showtimes when room filter changes
  useEffect(() => {
    if (selectedCinemaId) {
      loadShowtimes();
    }
  }, [selectedRoomId]);

  const loadCinemas = async () => {
    try {
      setLoadingCinemas(true);
      const data = await getAllCinemas();
      setCinemas(data);
    } catch (error) {
      console.error("Failed to load cinemas:", error);
      addNotification({
        type: "error",
        title: "Error",
        message: "Unable to load cinemas",
      });
    } finally {
      setLoadingCinemas(false);
    }
  };

  const loadRooms = async (cinemaId: string) => {
    try {
      setLoadingRooms(true);
      const response = await getRoomsByCinema(cinemaId);
      setRooms(response.data.result || []);
    } catch (error) {
      console.error("Failed to load rooms:", error);
      addNotification({
        type: "error",
        title: "Error",
        message: "Unable to load rooms",
      });
    } finally {
      setLoadingRooms(false);
    }
  };

  const loadShowtimes = async () => {
    if (!selectedCinemaId) return;

    try {
      setLoadingShowtimes(true);
      const data = await getAllShowtimes();
      console.log("All showtimes loaded:", data);

      // Filter by selected cinema and room
      let filteredShowtimes = data.filter(
        (showtime) => showtime.cinemaId === selectedCinemaId,
      );

      if (selectedRoomId) {
        filteredShowtimes = filteredShowtimes.filter(
          (showtime) => showtime.roomId === selectedRoomId,
        );
      }

      setAllShowtimes(filteredShowtimes);
    } catch (error) {
      console.error("Failed to load showtimes:", error);
      addNotification({
        type: "error",
        title: "Error",
        message: "Unable to load showtimes",
      });
    } finally {
      setLoadingShowtimes(false);
    }
  };

  // Sync cinema and room filters to URL params
  const updateUrlParams = (cinemaId: string | null, roomId: string | null) => {
    const params = new URLSearchParams();
    if (cinemaId) params.set("cinema", cinemaId);
    if (roomId) params.set("room", roomId);

    const queryString = params.toString();
    const newUrl = queryString
      ? `${ROUTES.SHOWTIMES}?${queryString}`
      : ROUTES.SHOWTIMES;
    navigate(newUrl, { replace: true });
  };

  const handleSelectCinema = (cinemaId: string) => {
    setSelectedCinemaId(cinemaId);
    setSelectedRoomId(null);
    updateUrlParams(cinemaId, null);
  };

  const handleBackToCinemas = () => {
    setSelectedCinemaId(null);
    setSelectedRoomId(null);
    setAllShowtimes([]);
    updateUrlParams(null, null);
  };

  const handleShowtimeClick = (showtime: ShowtimeResponse) => {
    setSelectedShowtimeId(showtime.id);
    setDetailModalOpen(true);
  };

  const handleCloseDetailModal = () => {
    setDetailModalOpen(false);
    setSelectedShowtimeId(null);
  };

  const selectedCinema = cinemas.find((c) => c.id === selectedCinemaId);

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <PageHeader
        title="Movie Schedule"
        description={
          selectedCinema
            ? `View schedule for ${selectedCinema.name}`
            : "View movie schedules by cinema"
        }
      />

      {/* Cinema Selection or Calendar View */}
      {!selectedCinemaId ? (
        <CinemaSelector
          cinemas={cinemas}
          selectedCinemaId={selectedCinemaId}
          onSelectCinema={handleSelectCinema}
          loading={loadingCinemas}
        />
      ) : (
        <div className="space-y-6">
          {/* Back Button & Cinema Info */}
          <div className="flex items-center justify-between">
            <Button
              variant="outline"
              onClick={handleBackToCinemas}
              className="gap-2"
            >
              <ArrowLeft className="h-4 w-4" />
              Back to cinemas
            </Button>

            <div className="flex items-center gap-3">
              <div className="text-right">
                <h3 className="text-lg font-semibold text-gray-900">
                  {selectedCinema?.name}
                </h3>
                <p className="text-sm text-gray-600">
                  {selectedCinema?.address}
                </p>
              </div>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex items-center gap-3">
            <Button onClick={() => setCreateDialogOpen(true)} className="gap-2">
              <Plus className="h-4 w-4" />
              Create Showtime
            </Button>
            <Button
              variant="outline"
              onClick={() => setEditDialogOpen(true)}
              className="gap-2"
            >
              <Edit className="h-4 w-4" />
              Edit Showtime
            </Button>
            <Button
              variant="outline"
              onClick={() => setDeleteDialogOpen(true)}
              className="gap-2 text-red-600 hover:bg-red-50 hover:text-red-700"
            >
              <Trash2 className="h-4 w-4" />
              Delete Showtime
            </Button>
          </div>

          {/* Room Filter */}
          <RoomFilter
            rooms={rooms}
            selectedRoomId={selectedRoomId}
            onSelectRoom={(roomId) => {
              setSelectedRoomId(roomId);
              updateUrlParams(selectedCinemaId, roomId);
            }}
            loading={loadingRooms}
          />

          {/* Calendar */}
          <ShowtimeCalendar
            showtimes={allShowtimes}
            currentMonth={currentMonth}
            onMonthChange={setCurrentMonth}
            onShowtimeClick={handleShowtimeClick}
            loading={loadingShowtimes}
          />

          {/* Stats */}
          <div className="grid gap-4 md:grid-cols-4">
            <div className="rounded-lg border border-gray-200 bg-white p-4">
              <p className="text-sm text-gray-600">Total Showtimes</p>
              <p className="mt-1 text-2xl font-bold text-gray-900">
                {allShowtimes.length}
              </p>
            </div>
            <div className="rounded-lg border border-gray-200 bg-white p-4">
              <p className="text-sm text-gray-600">Scheduled</p>
              <p className="mt-1 text-2xl font-bold text-blue-600">
                {allShowtimes.filter((s) => s.status === "SCHEDULED").length}
              </p>
            </div>
            <div className="rounded-lg border border-gray-200 bg-white p-4">
              <p className="text-sm text-gray-600">Ongoing</p>
              <p className="mt-1 text-2xl font-bold text-green-600">
                {allShowtimes.filter((s) => s.status === "ONGOING").length}
              </p>
            </div>
            <div className="rounded-lg border border-gray-200 bg-white p-4">
              <p className="text-sm text-gray-600">Completed</p>
              <p className="mt-1 text-2xl font-bold text-gray-600">
                {allShowtimes.filter((s) => s.status === "COMPLETED").length}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Showtime Detail Modal */}
      <ShowtimeDetailModal
        showtimeId={selectedShowtimeId}
        open={detailModalOpen}
        onClose={handleCloseDetailModal}
      />

      {/* Create Showtime Dialog */}
      {selectedCinemaId && selectedCinema && (
        <CreateShowtimeDialog
          open={createDialogOpen}
          onClose={() => setCreateDialogOpen(false)}
          cinemaId={selectedCinemaId}
          cinemaName={selectedCinema.name}
          cinemaBuffer={selectedCinema.buffer}
          rooms={rooms}
          onSuccess={loadShowtimes}
        />
      )}

      {/* Edit Showtime Dialog */}
      {selectedCinemaId && selectedCinema && (
        <EditShowtimeDialog
          open={editDialogOpen}
          onClose={() => setEditDialogOpen(false)}
          cinemaName={selectedCinema.name}
          cinemaBuffer={selectedCinema.buffer}
          rooms={rooms}
          showtimes={allShowtimes}
          onSuccess={loadShowtimes}
        />
      )}

      {/* Delete Showtime Dialog */}
      {selectedCinema && (
        <DeleteShowtimeDialog
          open={deleteDialogOpen}
          onClose={() => setDeleteDialogOpen(false)}
          cinemaName={selectedCinema.name}
          showtimes={allShowtimes}
          onSuccess={loadShowtimes}
        />
      )}
    </div>
  );
}

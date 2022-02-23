import React from 'react';
import { Card, Image, Icon, Placeholder, Label } from 'semantic-ui-react'
import moment from 'moment';

class EventCard extends React.Component {

    handleRatingChange = (e, { value }) => {
        let { OnPreferenceChange, event } = this.props;
        if (OnPreferenceChange) {
            OnPreferenceChange(event.id, event.myVote.canAttend, value);
        }
    }

    handleCanAttendChange = (e, { value }) => {
        let { OnPreferenceChange, event } = this.props;
        if (OnPreferenceChange) {
            OnPreferenceChange(event.id, value, event.myVote.rating);
        }
    }

    handleDeleteClicked = () => {
        let { OnDeleteClicked, event } = this.props;
        if (OnDeleteClicked) {
            OnDeleteClicked(event.id);
        }
    }

    render() {
        let { tmEventData } = this.props;

        let image = (
            <Image wrapped ui={false}>
                <Placeholder>
                    <Placeholder.Image />
                </Placeholder>
            </Image>
        );

        let content = (
            <Card.Content>
                <Placeholder fluid>
                    <Placeholder.Header>
                        <Placeholder.Line />
                    </Placeholder.Header>
                    <Placeholder.Paragraph>
                        <Placeholder.Line />
                        <Placeholder.Line />
                        <Placeholder.Line />
                        <Placeholder.Line />
                    </Placeholder.Paragraph>
                </Placeholder>
            </Card.Content>
        );

        if (tmEventData !== undefined) {

            let title = tmEventData.name;
            let description = tmEventData.info;
            let date = tmEventData.dates?.start?.dateTime;
            let thumbnailUrl = tmEventData.images?.[0]?.url;
            let venue = tmEventData._embedded?.venues?.[0]?.name;
            let city = tmEventData._embedded?.venues?.[0]?.city?.name;
            let url = tmEventData.url;

            if (description !== undefined) {
                let descriptionWords = description.split(' ');
                if (descriptionWords.length > 50) {
                    description = descriptionWords.slice(0, 50).join(' ') + ' ...';
                }
            }

            image = (
                <Image wrapped ui={false}>
                    <img src={thumbnailUrl} crossOrigin='anonymous' />
                </Image>
            );

            content = (
                <Card.Content>
                    <Card.Header
                        as='a'
                        href={url}
                        target='_blank' content={title} />
                    <Card.Meta>
                        {date && <span>{moment(date).format('MMM Do YYYY, h:mm a')}</span>}
                        {venue && <span>@{venue},</span>}
                        {city && <span>{city}</span>}
                    </Card.Meta>
                    <Card.Description>
                        {description}
                    </Card.Description>
                </Card.Content>
            );
        }

        let { votes } = this.props.event;
        const nonDraftVotes = votes.filter(vote => !vote.isDraft)

        const yesCount = nonDraftVotes.filter(vote => vote.canAttend === 'YES').length;
        const noCount = nonDraftVotes.filter(vote => vote.canAttend === 'NO').length;
        const maybeCount = nonDraftVotes.filter(vote => vote.canAttend === 'MAYBE').length;
        const avgExcitement = nonDraftVotes.length === 0
            ? '?'
            : nonDraftVotes.map(x => x.rating).reduce((a, b) => a + b) / nonDraftVotes.length;

        return (
            <Card>
                {image}
                {content}
                <Card.Content extra>
                    <Label.Group>
                        <Label icon='check' content={yesCount} />
                        <Label icon='cancel' content={noCount} />
                        <Label icon='help'content={maybeCount} />
                        <Label icon='heart' content={avgExcitement} />
                    </Label.Group>
                </Card.Content>
                {this.props.extra}
            </Card>
        );
    }
}

export default EventCard;